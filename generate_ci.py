import os

JAVA_SERVICES = [
    "search", "promotion", "customer", "inventory", "payment", "order", 
    "tax", "rating", "location", "storefront-bff", "backoffice-bff"
    , "product", "media", "payment-paypal", "webhook", "cart", "recommendation"
]

TEMPLATE = """name: {service} service ci

on:
  push:
    branches: ["**"]
    tags: ["v*.*.*"] # Bắt event khi push tag cho Staging
    paths:
      - "{service}/**"
      - ".github/workflows/actions/action.yaml"
      - ".github/workflows/{service}-ci.yaml"
      - "pom.xml"
  pull_request:
    branches: ["main"]
    paths:
      - "{service}/**"
      - ".github/workflows/actions/action.yaml"
      - ".github/workflows/{service}-ci.yaml"
      - "pom.xml"
  workflow_dispatch:

jobs:
  Build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: ./.github/workflows/actions

      - name: Run Maven Build Command
        run: mvn clean install -pl {service} -am -DskipTests
        
      - name: Upload Build Artifacts
        uses: actions/upload-artifact@v4
        with:
          name: build-assets-{service}
          path: |
            **/target/*.jar
            **/target/classes/
            **/target/generated-sources/
          retention-days: 1

      - name: Run Maven Checkstyle
        run: mvn checkstyle:checkstyle -pl {service} -am -Dcheckstyle.output.file={service}-checkstyle-result.xml

  Test:
    needs: Build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: ./.github/workflows/actions

      - name: Run Unit Tests & Generate JaCoCo Report
        run: |
          mvn clean verify \\
          org.jacoco:jacoco-maven-plugin:0.8.14:prepare-agent \\
          test \\
          org.jacoco:jacoco-maven-plugin:0.8.14:report \\
          -pl {service} -am -DskipTests=false

      - name: Upload Jacoco Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report-{service}
          path: {service}/target/site/jacoco/jacoco.xml
          retention-days: 1

  SonarCloud:
    needs: [Build, Test]
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - uses: ./.github/workflows/actions
      - name: Download Build Artifacts
        uses: actions/download-artifact@v4
        with:
          name: build-assets-{service}
      - name: Download Jacoco Report
        uses: actions/download-artifact@v4
        with:
          name: jacoco-report-{service}
          path: {service}/target/site/jacoco/
      - name: Analyze with sonar cloud
        id: sonar
        env:
          SONAR_TOKEN: ${{{{ secrets.SONAR_TOKEN }}}}
        run: >
          mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
          -pl {service} -am -f pom.xml
          -Dsonar.coverage.jacoco.xmlReportPaths={service}/target/site/jacoco/jacoco.xml

  Check-Coverage:
    needs: Test
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4
      - name: Download Jacoco Report
        uses: actions/download-artifact@v4
        with:
          name: jacoco-report-{service}
          path: target/jacoco-results
      - name: Add coverage report to PR
        id: jacoco_report
        uses: madrapps/jacoco-report@v1.6.1
        with:
          paths: ${{{{github.workspace}}}}/target/jacoco-results/jacoco.xml
          token: ${{{{secrets.GITHUB_TOKEN}}}}
          min-coverage-overall: 70
          min-coverage-changed-files: 60
          title: '{service_cap} Coverage Report'
          update-comment: false
      - name: Enforce Threshold
        run: |
          THRESHOLD=70
          COVERAGE=${{{{ steps.jacoco_report.outputs.coverage-overall }}}}
          if (( $(echo "$COVERAGE <= $THRESHOLD" | bc -l) )); then
            echo "Độ bao phủ code ($COVERAGE%) thấp hơn yêu cầu ($THRESHOLD%)!"
            exit 1
          fi

  # --- BẮT ĐẦU PHẦN MỚI THÊM CHO CD & GITOPS ---
  Docker-Build-Push:
    needs: [Check-Coverage, SonarCloud] # Đảm bảo pass test mới build docker
    runs-on: ubuntu-latest
    if: github.event_name != 'pull_request' # Không push image khi chỉ mở PR
    outputs:
      image_tag: ${{{{ steps.prep.outputs.TAG }}}}
    steps:
      - uses: actions/checkout@v4
      - name: Download Build Artifacts
        uses: actions/download-artifact@v4
        with:
          name: build-assets-{service}
          
      - name: Determine Tag
        id: prep
        run: |
          # Nếu là tag (VD: v1.2.3) -> lấy tên tag. Nếu là branch -> lấy short commit sha
          if [[ $GITHUB_REF == refs/tags/* ]]; then
            TAG=${{{{GITHUB_REF#refs/tags/}}}}
          else
            TAG=$(echo $GITHUB_SHA | cut -c1-7)
          fi
          echo "TAG=$TAG" >> $GITHUB_OUTPUT
          
      - name: Log in to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{{{ secrets.DOCKERHUB_USERNAME }}}}
          password: ${{{{ secrets.DOCKERHUB_TOKEN }}}}

      - name: Build and push Docker images
        uses: docker/build-push-action@v6
        with:
          context: ./{service}
          push: true
          tags: ${{{{ secrets.DOCKERHUB_USERNAME }}}}/yas-{service}:${{{{ steps.prep.outputs.TAG }}}}

  GitOps-Update-Manifest:
    needs: Docker-Build-Push
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Manifest Repository
        uses: actions/checkout@v4
        with:
          # Thay bằng repo chứa file K8s/Helm của nhóm bạn
          repository: 'https://github.com/Thang2005HCMUS/yas.git' 
          # Cần tạo Personal Access Token (PAT) trên Github và lưu vào secret
          token: ${{{{ secrets.GITHUB_TOKEN }}}} 
          ref: main

      - name: Update Image Tag in Manifest
        env:
          NEW_TAG: ${{{{ needs.Docker-Build-Push.outputs.image_tag }}}}
        run: |
          # Phân luồng theo branch/tag
          if [[ $GITHUB_REF == refs/tags/* ]]; then
            ENV_FOLDER="staging"
          elif [[ $GITHUB_REF == refs/heads/main ]]; then
            ENV_FOLDER="dev"
          else
            echo "Feature branch, skipping GitOps update."
            exit 0
          fi

          # Dùng sed để replace tag trong file values.yaml của Helm (hoặc K8s yaml)
          # Lưu ý: Cần chỉnh lại đường dẫn file cho đúng với cấu trúc thư mục repo manifest của bạn
          FILE_PATH="$ENV_FOLDER/{service}/values.yaml"
          
          # Cập nhật dòng chứa tag thành tag mới
          sed -i "s/tag: .*/tag: $NEW_TAG/g" $FILE_PATH
          
          # Setup git
          git config user.name "GitHub Actions Bot"
          git config user.email "actions@github.com"
          
          # Commit & Push
          git add $FILE_PATH
          git commit -m "Update {service} image tag to $NEW_TAG for $ENV_FOLDER"
          git push origin main
"""

def main():
    output_dir = ".github/workflows"
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    for service in JAVA_SERVICES:
        service_cap = service.replace("-", " ").title().replace(" ", "")
        content = TEMPLATE.format(service=service, service_cap=service_cap)
        
        file_path = os.path.join(output_dir, f"{service}-ci.yaml")
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Generated: {file_path}")

if __name__ == "__main__":
    main()
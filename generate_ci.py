import os

# Danh sách các service Java có chung đặc điểm
JAVA_SERVICES = [
    "search", "promotion", "customer", "inventory", "payment", "order", 
    "tax", "rating", "location", "storefront-bff", "backoffice-bff", 
    "pricing", "product", "media", "payment-paypal", "webhook"
]

TEMPLATE = """name: {service} service ci

on:
  push:
    branches: ["**"]
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
    outputs:
      service_name: ${{{{ steps.detect.outputs.SERVICE_NAME }}}}
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Setup Environment
        uses: ./.github/workflows/actions

      - name: Detect Service Name
        id: detect
        run: |
          FILE="${{GITHUB_WORKFLOW_REF##*/}}"
          FILE="${{FILE%@*}}"
          SERVICE="${{FILE%-ci.yaml}}"
          echo "SERVICE_NAME=$SERVICE" >> $GITHUB_ENV
          echo "SERVICE_NAME=$SERVICE" >> $GITHUB_OUTPUT

      - name: Run Maven Build & Test
        run: mvn clean install -pl {service} -am

      - name: Run Maven Checkstyle
        run: mvn checkstyle:checkstyle -pl {service} -am -Dcheckstyle.output.file={service}-checkstyle-result.xml

      - name: Upload Checkstyle Result
        uses: jwgmeligmeyling/checkstyle-github-action@master
        with:
          path: '**/{service}-checkstyle-result.xml'

      - name: Test Results Reporter
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: {service_cap}-Unit-Test-Results
          path: "{service}/**/*-reports/TEST*.xml"
          reporter: java-junit

      - name: Upload Jacoco Report Artifact
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report-{service}
          path: {service}/target/site/jacoco/jacoco.xml
          retention-days: 1

      - name: Analyze with SonarCloud
        id: sonar
        continue-on-error: true
        env:
          SONAR_TOKEN: ${{{{ secrets.SONAR_TOKEN }}}}
        run: >
          mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
          -pl {service} -am -f pom.xml
          -Dsonar.coverage.jacoco.xmlReportPaths={service}/target/site/jacoco/jacoco.xml

      - name: Build and push Docker images
        if: ${{{{ github.ref == 'refs/heads/main' }}}}
        run: echo "Pushing Docker image for {service}..."
        # Thêm các step docker build/push tại đây nếu cần

  Check-Coverage:
    needs: Build
    runs-on: ubuntu-latest
    if: github.event_name == 'pull_request' || github.ref == 'refs/heads/main'
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Download Jacoco Report
        uses: actions/download-artifact@v4
        with:
          name: jacoco-report-{service}
          path: target/jacoco-data

      - name: Add Coverage Report to PR
        id: jacoco_reporter
        uses: madrapps/jacoco-report@v1.6.1
        with:
          paths: ${{{{ github.workspace }}}}/target/jacoco-data/jacoco.xml
          token: ${{{{ secrets.GITHUB_TOKEN }}}}
          min-coverage-overall: 80
          min-coverage-changed-files: 60
          title: '{service_cap} Coverage Report'
          update-comment: true

      - name: Fail if coverage is below threshold
        if: steps.jacoco_reporter.outputs.coverage-overall < 80
        run: |
          echo "❌ Overall coverage is below 80%!"
          exit 1
"""

def generate_ci_files():
    output_dir = ".github/workflows"
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
        
    for service in JAVA_SERVICES:
        file_name = f"{service}-ci.yaml"
        file_path = os.path.join(output_dir, file_name)
        
        content = TEMPLATE.format(
            service=service, 
            service_cap=service.capitalize()
        )
        
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Generated: {file_path}")

if __name__ == "__main__":
    generate_ci_files()
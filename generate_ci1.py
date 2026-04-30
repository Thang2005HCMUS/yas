import os

# Danh sách các service Java cần tạo CI
JAVA_SERVICES = [
    "search", "promotion", "customer", "inventory", "payment", "order", 
    "tax", "rating", "location", "storefront-bff", "backoffice-bff", 
    "pricing", "product", "media", "payment-paypal", "webhook", "sampledata", "cart", "recommendation"
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
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: ./.github/workflows/actions

      - name: Run Maven Build Command
        run: mvn clean install -pl {service} -am -DskipTests

      - name: Run Maven Checkstyle
        run: mvn checkstyle:checkstyle -pl {service} -am -Dcheckstyle.output.file={service}-checkstyle-result.xml

      - name: Upload Checkstyle Result
        uses: jwgmeligmeyling/checkstyle-github-action@master
        with:
          path: '**/{service}-checkstyle-result.xml'

      - name: Log in to the Container registry
        if: ${{{{ github.ref == 'refs/heads/main' }}}}
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{{{ github.actor }}}}
          password: ${{{{ secrets.GITHUB_TOKEN }}}}

      - name: Build and push Docker images
        if: ${{{{ github.ref == 'refs/heads/main' }}}}
        uses: docker/build-push-action@v6
        with:
          context: ./{service}
          push: true
          tags: ghcr.io/nashtech-garage/yas-{service}:latest

  Test:
    needs: Build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: ./.github/workflows/actions

      - name: Run Unit Tests
        run: mvn test -pl {service} -am

      - name: Test Results Summary (Tab)
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: {service_cap}-Unit-Test-Results
          path: "{service}/**/*-reports/TEST*.xml"
          reporter: java-junit

      - name: Write Test Summary to Job
        if: always()
        run: |
          echo "## 🧪 Test Result: {service}" >> $GITHUB_STEP_SUMMARY
          TEST_FILES=$(find {service}/target/surefire-reports -name "TEST-*.xml" 2>/dev/null || true)
          if [ -z "$TEST_FILES" ]; then
            echo "❌ Không tìm thấy file kết quả test." >> $GITHUB_STEP_SUMMARY
          else
            TOTAL_TESTS=0; TOTAL_FAILURES=0; TOTAL_ERRORS=0; TOTAL_SKIPPED=0
            for FILE in $TEST_FILES; do
              TESTS=$(grep -oE 'tests="[0-9]+"' "$FILE" | cut -d'"' -f2 | head -1)
              FAILURES=$(grep -oE 'failures="[0-9]+"' "$FILE" | cut -d'"' -f2 | head -1)
              ERRORS=$(grep -oE 'errors="[0-9]+"' "$FILE" | cut -d'"' -f2 | head -1)
              SKIPPED=$(grep -oE 'skipped="[0-9]+"' "$FILE" | cut -d'"' -f2 | head -1)
              TOTAL_TESTS=$((TOTAL_TESTS + TESTS))
              TOTAL_FAILURES=$((TOTAL_FAILURES + FAILURES))
              TOTAL_ERRORS=$((TOTAL_ERRORS + ERRORS))
              TOTAL_SKIPPED=$((TOTAL_SKIPPED + SKIPPED))
            done
            TOTAL_PASSED=$((TOTAL_TESTS - TOTAL_FAILURES - TOTAL_ERRORS - TOTAL_SKIPPED))
            echo "| Metric | Count |" >> $GITHUB_STEP_SUMMARY
            echo "| :--- | :--- |" >> $GITHUB_STEP_SUMMARY
            echo "| ✅ Passed | $TOTAL_PASSED |" >> $GITHUB_STEP_SUMMARY
            echo "| ❌ Failures | $TOTAL_FAILURES |" >> $GITHUB_STEP_SUMMARY
            echo "| ⚠️ Errors | $TOTAL_ERRORS |" >> $GITHUB_STEP_SUMMARY
            echo "| ⏭️ Skipped | $TOTAL_SKIPPED |" >> $GITHUB_STEP_SUMMARY
            echo "| **Total** | **$TOTAL_TESTS** |" >> $GITHUB_STEP_SUMMARY
          fi

      - name: Upload Jacoco Report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report-{service}
          path: {service}/target/site/jacoco/jacoco.xml
          retention-days: 1

  SonarCloud:
    needs: Test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - uses: ./.github/workflows/actions
      - name: Download Jacoco Report
        uses: actions/download-artifact@v4
        with:
          name: jacoco-report-{service}
          path: {service}/target/site/jacoco/
      - name: Analyze with sonar cloud
        id: sonar
        continue-on-error: true
        env:
          SONAR_TOKEN: ${{{{ secrets.SONAR_TOKEN }}}}
        run: >
          mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
          -pl {service} -am -f pom.xml
          -Dsonar.coverage.jacoco.xmlReportPaths={service}/target/site/jacoco/jacoco.xml
      - name: SonarCloud Summary
        if: always()
        run: |
          if [ "${{{{ steps.sonar.outcome }}}}" = "success" ]; then ICON="🟢"; STATUS="PASS"; else ICON="🔴"; STATUS="FAIL"; fi
          {{
            echo "## SonarCloud Report: {service}"
            echo "| Item | Value |"
            echo "|------|-------|"
            echo "| Status | $ICON **$STATUS** |"
            echo "### Dashboard"
            echo "https://sonarcloud.io/dashboard?id=<YOUR_PROJECT_KEY>"
          }} >> $GITHUB_STEP_SUMMARY

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
          min-coverage-overall: 80
          min-coverage-changed-files: 60
          title: '{service_cap} Coverage Report'
          update-comment: true

      - name: Write Coverage Summary
        if: always()
        run: |
          # Lấy giá trị, nếu trống thì mặc định là 0
          COVERAGE="${{{{ steps.jacoco_report.outputs.coverage-overall }}}}"
          CHANGED="${{{{ steps.jacoco_report.outputs.coverage-changed-files }}}}"
          
          [ -z "$COVERAGE" ] && COVERAGE=0
          [ -z "$CHANGED" ] && CHANGED=0
          
          THRESHOLD=80
          
          # So sánh số thực
          if (( $(echo "$COVERAGE >= $THRESHOLD" | bc -l) )); then
            ICON="✅"
            STATUS="PASSED"
          else
            ICON="❌"
            STATUS="FAILED"
          fi

          echo "## 📊 Coverage Summary: {service}" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          echo "| Metric | Value | Threshold | Status |" >> $GITHUB_STEP_SUMMARY
          echo "|--------|-------|-----------|--------|" >> $GITHUB_STEP_SUMMARY
          echo "| Overall Coverage | $COVERAGE% | $THRESHOLD% | $ICON $STATUS |" >> $GITHUB_STEP_SUMMARY
          echo "| Changed Files | $CHANGED% | 60% | - |" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          echo "Vui lòng kiểm tra chi tiết trong phần bình luận của Pull Request." >> $GITHUB_STEP_SUMMARY

      - name: Enforce Threshold
        run: |
          COVERAGE="${{{{ steps.jacoco_report.outputs.coverage-overall }}}}"
          [ -z "$COVERAGE" ] && COVERAGE=0
          if (( $(echo "$COVERAGE < 80" | bc -l) )); then
            echo "Độ bao phủ code ($COVERAGE%) thấp hơn yêu cầu (80%)!"
            exit 1
          fi
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
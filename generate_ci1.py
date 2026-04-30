import os

# =========================
# Services Java cần tạo CI
# =========================
JAVA_SERVICES = [
    "search", "promotion", "customer", "inventory", "payment", "order",
    "tax", "rating", "location", "storefront-bff", "backoffice-bff",
    "pricing", "product", "media", "payment-paypal", "webhook",
    "sampledata", "cart", "recommendation"
]

# =========================
# Template Workflow CI
# =========================
TEMPLATE = r"""name: {service} service ci

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

      - name: Maven Build
        run: mvn clean install -pl {service} -am -DskipTests

      - name: Checkstyle
        run: mvn checkstyle:checkstyle -pl {service} -am -Dcheckstyle.output.file={service}-checkstyle-result.xml

      - name: Upload Checkstyle Result
        uses: jwgmeligmeyling/checkstyle-github-action@master
        with:
          path: '**/{service}-checkstyle-result.xml'

      - name: Login GHCR
        if: ${{ github.ref == 'refs/heads/main' }}
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build Docker Image
        if: ${{ github.ref == 'refs/heads/main' }}
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

      - name: Run Tests + Generate Coverage
        run: mvn verify -pl {service} -am

      - name: Verify Jacoco Exists
        run: ls -R {service}/target/site/jacoco || true

      - name: Upload Jacoco Report
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report-{service}
          path: {service}/target/site/jacoco/jacoco.xml
          retention-days: 1

      - name: Test Report
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: {service_cap}-Unit-Test
          path: "{service}/**/*-reports/TEST*.xml"
          reporter: java-junit

  SonarCloud:
    needs: Test
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: ./.github/workflows/actions

      - name: Download Jacoco
        uses: actions/download-artifact@v4
        with:
          name: jacoco-report-{service}
          path: {service}/target/site/jacoco

      - name: Run SonarCloud
        id: sonar
        continue-on-error: true
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: >
          mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar
          -pl {service} -am
          -Dsonar.projectKey=nashtech-garage_yas-{service}
          -Dsonar.organization=nashtech-garage
          -Dsonar.host.url=https://sonarcloud.io
          -Dsonar.coverage.jacoco.xmlReportPaths={service}/target/site/jacoco/jacoco.xml

      - name: Sonar Summary
        if: always()
        run: |
          if [ "${{{{ steps.sonar.outcome }}}}" = "success" ]; then
            ICON="🟢"
            STATUS="PASS"
          else
            ICON="🔴"
            STATUS="FAIL"
          fi

          echo "## SonarCloud {service}" >> $GITHUB_STEP_SUMMARY
          echo "| Status | Result |" >> $GITHUB_STEP_SUMMARY
          echo "|--------|--------|" >> $GITHUB_STEP_SUMMARY
          echo "| Scan | $ICON $STATUS |" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          echo "https://sonarcloud.io/project/overview?id=nashtech-garage_yas-{service}" >> $GITHUB_STEP_SUMMARY

  Coverage:
    needs: Test
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Download Jacoco
        uses: actions/download-artifact@v4
        with:
          name: jacoco-report-{service}
          path: target/jacoco-results

      - name: Coverage Report
        id: jacoco
        uses: madrapps/jacoco-report@v1.6.1
        with:
          paths: target/jacoco-results/jacoco.xml
          token: ${{ secrets.GITHUB_TOKEN }}
          min-coverage-overall: 80
          min-coverage-changed-files: 60
          title: "{service_cap} Coverage Report"
          update-comment: true

      - name: Coverage Summary
        if: always()
        run: |
          COVERAGE="${{{{ steps.jacoco.outputs.coverage-overall }}}}"
          CHANGED="${{{{ steps.jacoco.outputs.coverage-changed-files }}}}"

          [ -z "$COVERAGE" ] && COVERAGE=0
          [ -z "$CHANGED" ] && CHANGED=0

          echo "## Coverage {service}" >> $GITHUB_STEP_SUMMARY
          echo "| Metric | Value |" >> $GITHUB_STEP_SUMMARY
          echo "|--------|------:|" >> $GITHUB_STEP_SUMMARY
          echo "| Overall | $COVERAGE% |" >> $GITHUB_STEP_SUMMARY
          echo "| Changed Files | $CHANGED% |" >> $GITHUB_STEP_SUMMARY
"""

# =========================
# Generate files
# =========================
def main():
    output_dir = ".github/workflows"
    os.makedirs(output_dir, exist_ok=True)

    for service in JAVA_SERVICES:
        service_cap = service.replace("-", " ").title().replace(" ", "")

        content = TEMPLATE.format(
            service=service,
            service_cap=service_cap
        )

        file_path = os.path.join(output_dir, f"{service}-ci.yaml")

        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)

        print("Generated:", file_path)


if __name__ == "__main__":
    main()
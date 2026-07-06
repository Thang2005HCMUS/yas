"""Generate the GitHub Actions CI/GitOps workflows used by the YAS demo.

Do not edit the generated ``*-ci.yaml`` files by hand.  Change this file and run:

    python generate_ci1.py
"""

from dataclasses import dataclass
from pathlib import Path
from textwrap import dedent


ROOT = Path(__file__).resolve().parent
WORKFLOW_DIR = ROOT / ".github" / "workflows"


@dataclass(frozen=True)
class Service:
    """A deployable service and the repository paths used to build/deploy it."""

    name: str
    kind: str
    source_dir: str | None = None
    image_name: str | None = None

    @property
    def source(self) -> str:
        return self.source_dir or self.name

    @property
    def image(self) -> str:
        return self.image_name or f"yas-{self.name}"

    @property
    def values_file(self) -> str:
        return f"k8s/charts/{self.name}/values.yaml"


# These are the services retained for the DevOps/CD assignment.  The UI source
# directory and its Helm release have different names in the upstream project.
SERVICES = [
    Service("product", "java"),
    Service("cart", "java"),
    Service("order", "java"),
    Service("customer", "java"),
    Service("inventory", "java"),
    Service("tax", "java"),
    Service("media", "java"),
    Service("search", "java"),
    Service("storefront-bff", "java"),
    Service("backoffice-bff", "java"),
    Service("storefront-ui", "node", source_dir="storefront", image_name="yas-storefront"),
    Service("backoffice-ui", "node", source_dir="backoffice", image_name="yas-backoffice"),
    Service("swagger-ui", "container"),
    Service("sampledata", "java"),
]


JAVA_TEMPLATE = r"""
name: __SERVICE__ CI GitOps

on:
  push:
    branches: ["**"]
    paths:
      - "__SOURCE__/**"
      - "common-library/**"
      - "pom.xml"
      - "generate_ci1.py"
      - ".github/workflows/__SERVICE__-ci.yaml"
  pull_request:
    branches: ["main"]
    paths:
      - "__SOURCE__/**"
      - "common-library/**"
      - "pom.xml"
      - "generate_ci1.py"
      - ".github/workflows/__SERVICE__-ci.yaml"
  workflow_dispatch:

permissions:
  contents: write

concurrency:
  group: __SERVICE__-${{ github.ref }}
  cancel-in-progress: false

jobs:
  build-test-publish:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout source
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up JDK 25
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "25"
          cache: maven

      - name: Build __SERVICE__
        run: mvn clean package -pl __SOURCE__ -am

      - name: Test __SERVICE__
        run: mvn test -pl __SOURCE__ -am
__DOCKER_STEPS__
"""


NODE_TEMPLATE = r"""
name: __SERVICE__ CI GitOps

on:
  push:
    branches: ["**"]
    paths:
      - "__SOURCE__/**"
      - "generate_ci1.py"
      - ".github/workflows/__SERVICE__-ci.yaml"
  pull_request:
    branches: ["main"]
    paths:
      - "__SOURCE__/**"
      - "generate_ci1.py"
      - ".github/workflows/__SERVICE__-ci.yaml"
  workflow_dispatch:

permissions:
  contents: write

concurrency:
  group: __SERVICE__-${{ github.ref }}
  cancel-in-progress: false

jobs:
  build-test-publish:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout source
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Set up Node.js 20
        uses: actions/setup-node@v4
        with:
          node-version: "20"
          cache: npm
          cache-dependency-path: __SOURCE__/package-lock.json

      - name: Install dependencies
        working-directory: __SOURCE__
        run: npm ci

      # The YAS UI packages do not currently define an npm test script. Lint is
      # the available automated code check, so it is the test gate for the UI.
      - name: Test __SERVICE__ (lint)
        working-directory: __SOURCE__
        run: npm run lint

      - name: Build __SERVICE__
        working-directory: __SOURCE__
        run: npm run build
__DOCKER_STEPS__
"""


CONTAINER_TEMPLATE = r"""
name: __SERVICE__ CI GitOps

on:
  push:
    branches: ["**"]
    paths:
      - "__SOURCE__/**"
      - "generate_ci1.py"
      - ".github/workflows/__SERVICE__-ci.yaml"
  workflow_dispatch:

permissions:
  contents: write

concurrency:
  group: __SERVICE__-${{ github.ref }}
  cancel-in-progress: false

jobs:
  build-publish:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout source
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
__DOCKER_STEPS__
"""


DOCKER_STEPS = r"""

      - name: Set up Docker Buildx
        if: github.event_name == 'push'
        uses: docker/setup-buildx-action@v3

      - name: Log in to Docker Hub
        if: github.event_name == 'push'
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Build and push Docker image
        if: github.event_name == 'push'
        uses: docker/build-push-action@v6
        with:
          context: ./__SOURCE__
          push: true
          tags: ${{ secrets.DOCKERHUB_USERNAME }}/__IMAGE__:${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
"""


GITOPS_STEPS = r"""

      - name: Update Helm image for ArgoCD
        if: github.event_name == 'push'
        env:
          IMAGE_REPOSITORY: ${{ secrets.DOCKERHUB_USERNAME }}/__IMAGE__
          IMAGE_TAG: ${{ github.sha }}
          VALUES_FILE: __VALUES_FILE__
        run: |
          python - <<'PY'
          import os
          from pathlib import Path

          values_file = Path(os.environ["VALUES_FILE"])
          repository = os.environ["IMAGE_REPOSITORY"]
          tag = os.environ["IMAGE_TAG"]
          lines = values_file.read_text(encoding="utf-8").splitlines(keepends=True)

          image_indent = None
          repository_updated = False
          tag_updated = False
          for index, line in enumerate(lines):
              stripped = line.strip()
              indent = len(line) - len(line.lstrip())
              if image_indent is None:
                  if stripped == "image:":
                      image_indent = indent
                  continue
              if stripped and indent <= image_indent:
                  break
              if stripped.startswith("repository:"):
                  lines[index] = f"{' ' * indent}repository: {repository}\n"
                  repository_updated = True
              elif stripped.startswith("tag:"):
                  lines[index] = f"{' ' * indent}tag: \"{tag}\"\n"
                  tag_updated = True

          if not (repository_updated and tag_updated):
              raise SystemExit(f"Could not update image.repository and image.tag in {values_file}")
          values_file.write_text("".join(lines), encoding="utf-8")
          PY

      - name: Commit Helm manifest change
        if: github.event_name == 'push'
        env:
          IMAGE_TAG: ${{ github.sha }}
          VALUES_FILE: __VALUES_FILE__
        run: |
          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
          git add "$VALUES_FILE"
          if git diff --cached --quiet; then
            echo "Helm values already use image tag $IMAGE_TAG"
            exit 0
          fi
          git commit -m "chore(gitops): deploy __SERVICE__ $IMAGE_TAG [skip ci]"

          for attempt in 1 2 3; do
            if git push origin "HEAD:${GITHUB_REF_NAME}"; then
              exit 0
            fi
            echo "Push attempt $attempt failed; rebasing onto origin/${GITHUB_REF_NAME}"
            git fetch origin "$GITHUB_REF_NAME"
            git rebase "origin/${GITHUB_REF_NAME}"
          done
          echo "Unable to push the Helm manifest update after 3 attempts"
          exit 1
"""


def docker_steps(service: Service, has_values_file: bool) -> str:
    """Return publish steps, plus GitOps steps when a Helm values file exists."""

    steps = DOCKER_STEPS
    if has_values_file:
        steps += GITOPS_STEPS
    return steps


def render(service: Service, has_dockerfile: bool, has_values_file: bool) -> str:
    """Render one service workflow."""

    templates = {
        "java": JAVA_TEMPLATE,
        "node": NODE_TEMPLATE,
        "container": CONTAINER_TEMPLATE,
    }
    template = templates[service.kind]
    publish_steps = docker_steps(service, has_values_file) if has_dockerfile else ""
    # Insert the optional block first because it contains the other placeholders.
    content = dedent(template).lstrip().replace(
        "__DOCKER_STEPS__", publish_steps.rstrip()
    )
    replacements = {
        "__SERVICE__": service.name,
        "__SOURCE__": service.source,
        "__IMAGE__": service.image,
        "__VALUES_FILE__": service.values_file,
    }
    for placeholder, value in replacements.items():
        content = content.replace(placeholder, value)
    return content.rstrip() + "\n"


def main() -> None:
    WORKFLOW_DIR.mkdir(parents=True, exist_ok=True)

    generated = 0
    skipped = 0
    for service in SERVICES:
        source_path = ROOT / service.source
        if not source_path.is_dir():
            print(f"WARNING: skipping {service.name}: source folder '{service.source}' does not exist")
            skipped += 1
            continue

        dockerfile = source_path / "Dockerfile"
        values_file = ROOT / service.values_file
        has_dockerfile = dockerfile.is_file()
        has_values_file = values_file.is_file()
        if not has_dockerfile:
            print(f"WARNING: {service.name}: no Dockerfile; generating build/test only")
        if has_dockerfile and not has_values_file:
            print(f"WARNING: {service.name}: no Helm values file; skipping GitOps update")

        workflow_file = WORKFLOW_DIR / f"{service.name}-ci.yaml"
        workflow_file.write_text(
            render(service, has_dockerfile, has_values_file), encoding="utf-8"
        )
        print(f"Generated: {workflow_file.relative_to(ROOT)}")
        generated += 1

    # The upstream UI workflows use source-folder names.  Their replacements
    # above use deployment names, so remove these two aliases to avoid duplicate CI.
    for legacy_name in ("storefront-ci.yaml", "backoffice-ci.yaml"):
        legacy_file = WORKFLOW_DIR / legacy_name
        if legacy_file.exists():
            legacy_file.unlink()
            print(f"Removed legacy duplicate: {legacy_file.relative_to(ROOT)}")

    print(f"Done: generated {generated} workflow(s), skipped {skipped} service(s)")


if __name__ == "__main__":
    main()

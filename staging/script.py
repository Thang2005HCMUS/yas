from pathlib import Path

output_file = "content.txt"

with open(output_file, "w", encoding="utf-8") as out:
    # Tìm tất cả file .yaml và .yml
    yaml_files = sorted(
        list(Path(".").rglob("*.yaml")) +
        list(Path(".").rglob("*.yml"))
    )

    for file_path in yaml_files:
        relative_path = file_path.as_posix()

        out.write(f"=========={relative_path}==========\n")

        try:
            content = file_path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            # fallback nếu file không phải utf-8
            content = file_path.read_text(encoding="utf-8", errors="replace")

        out.write(content)
        out.write("\n\n")
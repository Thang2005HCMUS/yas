import os

def gather_files_content(output_filename="content.txt"):
    # Lấy đường dẫn của thư mục hiện tại (nơi đặt script)
    root_dir = os.path.dirname(os.path.abspath(__file__))
    
    with open(output_filename, "w", encoding="utf-8") as outfile:
        # os.walk sẽ đi xuyên qua tất cả các thư mục con (cấp n)
        for root, dirs, files in os.walk(root_dir):
            for file in files:
                # Bỏ qua chính file output và chính script này để tránh lặp vô tận
                if file == output_filename or file == os.path.basename(__file__):
                    continue
                
                file_path = os.path.join(root, file)
                
                # Tính toán đường dẫn tương đối (Relative Path)
                relative_path = os.path.relpath(file_path, root_dir)
                # Thêm ./ vào đầu cho đúng định dạng bạn yêu cầu
                formatted_path = f"./{relative_path}"
                
                try:
                    with open(file_path, "r", encoding="utf-8", errors="ignore") as infile:
                        content = infile.read()
                        
                    # Ghi vào file tổng theo định dạng yêu cầu
                    outfile.write(f"== {formatted_path} ==\n")
                    outfile.write(content)
                    outfile.write("\n\n") # Thêm dòng trống để dễ phân biệt các file
                    
                    print(f"Đã đọc: {formatted_path}")
                except Exception as e:
                    print(f"Lỗi khi đọc file {formatted_path}: {e}")

if __name__ == "__main__":
    print("Bắt đầu quét và gom nội dung file...")
    gather_files_content()
    print("-" * 30)
    print("Hoàn thành! Nội dung đã được lưu vào file content.txt")
import requests

def test_upload(url, image_path):
    """测试上传接口"""
    files = {"image": open(image_path, "rb")}
    try:
        response = requests.post(url, files=files)
        response.raise_for_status()  # 如果状态码不是 2xx，会抛出异常
        try:
            data = response.json()
            print(f"[SUCCESS] {url} 返回 JSON：{data}")
        except ValueError:
            print(f"[WARNING] {url} 返回非 JSON 内容：{response.text}")
    except requests.exceptions.HTTPError as e:
        print(f"[HTTP ERROR] {url} 状态码：{e.response.status_code}")
    except requests.exceptions.ConnectionError:
        print(f"[CONNECTION ERROR] 无法连接 {url}")
    except Exception as e:
        print(f"[ERROR] {url} 出现异常：{e}")

if __name__ == "__main__":
    image_path = "/root/data/youcai-vue/youcai-demo01/backend/routers/62700.jpg"

    urls = [
        "http://127.0.0.1:8000/api/upload",  # 直接访问 FastAPI
        "http://127.0.0.1/api/upload"        # 通过 Nginx 代理
    ]

    for url in urls:
        print(f"测试接口: {url}")
        test_upload(url, image_path)
        print("-" * 50)

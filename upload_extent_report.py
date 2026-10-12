import os
import sys
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload
def main():
    if len(sys.argv) != 3:
        raise ValueError(
            "Usage: upload_extent_report.py <report_path> <drive_file_id>"
        )
report_path = os.path.abspath(sys.argv[1])
    file_id = sys.argv[2].strip()
    credentials_path = os.environ.get("GOOGLE_DRIVE_CREDENTIALS")
if not os.path.isfile(report_path):
        raise FileNotFoundError(
            f"Extent HTML report not found: {report_path}"
        )
if not credentials_path or not os.path.isfile(credentials_path):
        raise FileNotFoundError(
            "Google Drive service-account credentials are unavailable."
        )
if not file_id:
        raise ValueError("Google Drive file ID is missing.")
credentials = Credentials.from_service_account_file(
credentials_path,
scopes=["https://www.googleapis.com/auth/drive"]
)
drive = build(
        "drive",
        "v3",
        credentials=credentials,
        cache_discovery=False
    )
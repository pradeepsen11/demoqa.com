import os
import sys
from pathlib import Path

from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload


SCOPES = ["https://www.googleapis.com/auth/drive"]
FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"


def upload_extent_report(report_path: str, drive_file_id: str) -> str:
    report = Path(report_path).expanduser().resolve()
    credentials_path = os.environ.get("GOOGLE_DRIVE_CREDENTIALS")
    file_id = drive_file_id.strip()

    if not report.is_file():
        raise FileNotFoundError(f"Extent HTML report not found: {report}")

    if report.suffix.lower() not in {".html", ".htm"}:
        raise ValueError(f"Expected an HTML report, received: {report.name}")

    if not credentials_path:
        raise EnvironmentError(
            "GOOGLE_DRIVE_CREDENTIALS is not set by Jenkins credentials binding."
        )

    credentials_file = Path(credentials_path)
    if not credentials_file.is_file():
        raise FileNotFoundError(
            f"Google service-account credential file not found: {credentials_file}"
        )

    if not file_id:
        raise ValueError("Google Drive target file ID is empty.")

    credentials = Credentials.from_service_account_file(
        str(credentials_file),
        scopes=SCOPES,
    )

    service = build(
        "drive",
        "v3",
        credentials=credentials,
        cache_discovery=False,
    )

    existing = service.files().get(
        fileId=file_id,
        fields="id,name,mimeType,webViewLink",
        supportsAllDrives=True,
    ).execute()

    if existing.get("mimeType") == FOLDER_MIME_TYPE:
        raise ValueError(
            "The configured Google Drive ID belongs to a folder, not an HTML file."
        )

    print(f"Updating existing Google Drive file: {existing.get('name', file_id)}")
    print(f"Local report: {report}")

    media = MediaFileUpload(
        str(report),
        mimetype="text/html",
        resumable=True,
    )

    updated = service.files().update(
        fileId=file_id,
        media_body=media,
        fields="id,name,mimeType,webViewLink",
        supportsAllDrives=True,
    ).execute()

    report_url = updated.get("webViewLink") or (
        f"https://drive.google.com/file/d/{updated['id']}/view"
    )

    print("UPLOAD_STATUS=SUCCESS")
    print(f"Drive file name: {updated.get('name', '')}")
    print(f"Drive file ID: {updated['id']}")
    print(f"Google Drive report URL: {report_url}")

    return report_url


def main() -> int:
    if len(sys.argv) != 3:
        print(
            "Usage: py -3 upload_extent_report.py "
            "<report_path> <existing_drive_file_id>",
            file=sys.stderr,
        )
        return 2

    try:
        upload_extent_report(sys.argv[1], sys.argv[2])
        return 0
    except Exception as exc:
        print(f"Google Drive upload failed: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())

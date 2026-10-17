import os
import sys
from pathlib import Path
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

SCOPES = ["https://www.googleapis.com/auth/drive"]
FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"


def upload_extent_report(report_path: str, folder_id: str) -> str:
    report = Path(report_path).expanduser().resolve()
    credentials_path = os.environ.get("GOOGLE_DRIVE_CREDENTIALS")
    folder_id = folder_id.strip()
    if not report.is_file() or report.suffix.lower() not in {".html", ".htm"} or report.stat().st_size == 0:
        raise ValueError(f"Missing, empty, or invalid HTML report: {report}")
    if not credentials_path or not Path(credentials_path).is_file():
        raise FileNotFoundError("GOOGLE_DRIVE_CREDENTIALS is missing or unreadable.")
    if not folder_id:
        raise ValueError("Google Drive folder ID is empty.")

    credentials = Credentials.from_service_account_file(credentials_path, scopes=SCOPES)
    drive = build("drive", "v3", credentials=credentials, cache_discovery=False)
    folder = drive.files().get(fileId=folder_id, fields="id,name,mimeType", supportsAllDrives=True).execute()
    if folder.get("mimeType") != FOLDER_MIME_TYPE:
        raise ValueError(f"Configured ID is not a Google Drive folder: {folder.get('name', folder_id)}")

    escaped_name = report.name.replace("\\", "\\\\").replace("'", "\\'")
    query = f"'{folder_id}' in parents and name = '{escaped_name}' and trashed = false"
    files = drive.files().list(
        q=query, pageSize=100, orderBy="modifiedTime desc",
        fields="files(id,name,mimeType,webViewLink)", supportsAllDrives=True,
        includeItemsFromAllDrives=True,
    ).execute().get("files", [])
    media = MediaFileUpload(str(report), mimetype="text/html", resumable=True)
    if files:
        target = files[0]
        if target.get("mimeType") == FOLDER_MIME_TYPE:
            raise ValueError("A folder exists with the report filename; expected an HTML file.")
        result = drive.files().update(
            fileId=target["id"], media_body=media,
            fields="id,name,mimeType,webViewLink", supportsAllDrives=True,
        ).execute()
        action = "Updated"
    else:
        result = drive.files().create(
            body={"name": report.name, "mimeType": "text/html", "parents": [folder_id]},
            media_body=media, fields="id,name,mimeType,webViewLink", supportsAllDrives=True,
        ).execute()
        action = "Created"
    file_id = result.get("id")
    if not file_id:
        raise RuntimeError("Drive API did not return the uploaded report ID.")
    url = result.get("webViewLink") or f"https://drive.google.com/file/d/{file_id}/view"
    print(f"{action} Google Drive report: {result.get('name', report.name)}")
    print(f"Google Drive report URL: {url}")
    return url


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: upload_extent_report.py <report_path> <google_drive_folder_id>", file=sys.stderr)
        raise SystemExit(2)
    try:
        upload_extent_report(sys.argv[1], sys.argv[2])
    except Exception as exc:
        print(f"Google Drive upload failed: {exc}", file=sys.stderr)
        raise SystemExit(1)

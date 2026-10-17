# Upload/update an Extent HTML report inside a Google Drive folder.
# Usage: python upload_extent_report_fixed.py <local_report_path> <drive_folder_id>
import os
import sys
import mimetypes
from google.oauth2.service_account import Credentials
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload
from googleapiclient.errors import HttpError

SCOPES = ["https://www.googleapis.com/auth/drive"]

def main():
    if len(sys.argv) != 3:
        raise SystemExit("Usage: upload_extent_report_fixed.py <report_path> <drive_folder_id>")

    report_path = os.path.abspath(sys.argv[1])
    folder_id = sys.argv[2].strip()
    credentials_path = os.environ.get("GOOGLE_DRIVE_CREDENTIALS", "").strip()

    if not os.path.isfile(report_path):
        raise SystemExit("ERROR: Report file does not exist: " + report_path)
    if not folder_id or "/" in folder_id or "?" in folder_id:
        raise SystemExit("ERROR: GOOGLE_DRIVE_FOLDER_ID must contain only the folder ID copied from the Drive folder URL.")
    if not credentials_path or not os.path.isfile(credentials_path):
        raise SystemExit("ERROR: GOOGLE_DRIVE_CREDENTIALS does not point to a readable service-account JSON file.")

    report_name = os.path.basename(report_path)
    creds = Credentials.from_service_account_file(credentials_path, scopes=SCOPES)
    service = build("drive", "v3", credentials=creds, cache_discovery=False)

    try:
        # Verify folder access and type. A 404 here means wrong folder ID or no access for this service account.
        folder = service.files().get(fileId=folder_id, fields="id,name,mimeType", supportsAllDrives=True).execute()
        if folder.get("mimeType") != "application/vnd.google-apps.folder":
            raise SystemExit("ERROR: Configured Drive ID is not a folder: " + folder.get("name", folder_id))

        escaped_name = report_name.replace("\\", "\\\\").replace("'", "\\'")
        query = "'{}' in parents and name = '{}' and trashed = false".format(folder_id, escaped_name)
        matches = service.files().list(
            q=query,
            spaces="drive",
            fields="files(id,name,mimeType,webViewLink)",
            pageSize=100,
            includeItemsFromAllDrives=True,
            supportsAllDrives=True,
        ).execute().get("files", [])

        mime_type = "text/html"
        media = MediaFileUpload(report_path, mimetype=mime_type, resumable=True)
        if matches:
            file_id = matches[0]["id"]
            updated = service.files().update(
                fileId=file_id,
                media_body=media,
                fields="id,name,mimeType,webViewLink",
                supportsAllDrives=True,
            ).execute()
            action = "Updated"
        else:
            metadata = {"name": report_name, "mimeType": mime_type, "parents": [folder_id]}
            updated = service.files().create(
                body=metadata,
                media_body=media,
                fields="id,name,mimeType,webViewLink",
                supportsAllDrives=True,
            ).execute()
            action = "Created"

        file_id = updated["id"]
        # Google Drive does not execute HTML as a hosted website; this is its file viewer URL.
        url = updated.get("webViewLink") or "https://drive.google.com/file/d/{}/view".format(file_id)
        print("{} Google Drive report: {}".format(action, report_name))
        print("Google Drive report URL: {}".format(url))
    except HttpError as exc:
        print("Google Drive upload failed: {}".format(exc), file=sys.stderr)
        raise SystemExit(1)

if __name__ == "__main__":
    main()

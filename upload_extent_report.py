"""Upload/update the selected Extent HTML report in a Google Drive folder.

Usage: python upload_extent_report.py <local_report_path> <drive_folder_id>
Credentials: GOOGLE_DRIVE_CREDENTIALS must point to a service-account JSON file.
"""
import os
import sys

SCOPES = ["https://www.googleapis.com/auth/drive"]
FOLDER_MIME = "application/vnd.google-apps.folder"
HTML_MIME = "text/html"


def _drive_error_message(exc, folder_id):
    status = getattr(getattr(exc, "resp", None), "status", None)
    if status == 404:
        return (
            "Google Drive folder was not found or is not shared with the Jenkins service account. "
            "Check the folder ID and share the folder with the service-account client_email as Editor. "
            "Configured folder ID ends with: ...{}".format(folder_id[-6:])
        )
    if status == 403:
        return "Google Drive denied access. Share the destination folder with the service-account client_email as Editor and verify Drive API access."
    return "Google Drive API request failed (HTTP {}). Check service-account permissions and Drive API configuration.".format(status or "unknown")


def upload_report(report_path, folder_id, credentials_path, service=None, media_factory=None):
    report_path = os.path.abspath(report_path)
    folder_id = folder_id.strip()
    if not os.path.isfile(report_path):
        raise ValueError("Local HTML report does not exist: {}".format(report_path))
    if not folder_id or "/" in folder_id or "?" in folder_id:
        raise ValueError("Destination must be the folder ID only, not a URL or file ID.")
    if not credentials_path or not os.path.isfile(credentials_path):
        raise ValueError("GOOGLE_DRIVE_CREDENTIALS must point to a readable service-account JSON file.")

    if service is None:
        from google.oauth2.service_account import Credentials
        from googleapiclient.discovery import build
        from googleapiclient.http import MediaFileUpload
        creds = Credentials.from_service_account_file(credentials_path, scopes=SCOPES)
        service = build("drive", "v3", credentials=creds, cache_discovery=False)
        media_factory = MediaFileUpload
    elif media_factory is None:
        media_factory = lambda path, mimetype, resumable: {"path": path, "mimetype": mimetype, "resumable": resumable}

    try:
        folder = service.files().get(fileId=folder_id, fields="id,name,mimeType", supportsAllDrives=True).execute()
        if folder.get("mimeType") != FOLDER_MIME:
            raise ValueError("Configured Drive ID is not a folder (found '{}'). Use the ID from /folders/<ID>.".format(folder.get("mimeType", "unknown")))

        name = os.path.basename(report_path)
        escaped = name.replace("\\", "\\\\").replace("'", "\\'")
        query = "'{}' in parents and name = '{}' and trashed = false".format(folder_id, escaped)
        listing = service.files().list(
            q=query, spaces="drive", fields="files(id,name,mimeType,webViewLink)", pageSize=100,
            includeItemsFromAllDrives=True, supportsAllDrives=True
        ).execute()
        matches = listing.get("files", [])
        media = media_factory(report_path, mimetype=HTML_MIME, resumable=True)
        if matches:
            file_id = matches[0]["id"]
            result = service.files().update(fileId=file_id, media_body=media,
                fields="id,name,mimeType,webViewLink", supportsAllDrives=True).execute()
            action = "Updated"
        else:
            result = service.files().create(
                body={"name": name, "mimeType": HTML_MIME, "parents": [folder_id]},
                media_body=media, fields="id,name,mimeType,webViewLink", supportsAllDrives=True
            ).execute()
            action = "Created"
        file_id = result["id"]
        # Drive's webViewLink opens its viewer; Drive does not host executable static HTML pages.
        url = result.get("webViewLink") or "https://drive.google.com/file/d/{}/view".format(file_id)
        return action, name, url
    except Exception as exc:
        if getattr(getattr(exc, "resp", None), "status", None) is not None:
            raise RuntimeError(_drive_error_message(exc, folder_id)) from None
        raise


def main(argv=None):
    argv = argv or sys.argv[1:]
    if len(argv) != 2:
        print("Usage: python upload_extent_report.py <local_report_path> <drive_folder_id>", file=sys.stderr)
        return 2
    credentials_path = os.environ.get("GOOGLE_DRIVE_CREDENTIALS", "").strip()
    folder_id = argv[1].strip()
    try:
        action, name, url = upload_report(argv[0], folder_id, credentials_path)
        print("{} Google Drive report: {}".format(action, name))
        print("Google Drive report URL: {}".format(url))
        return 0
    except (ValueError, RuntimeError) as exc:
        print("WARNING: Google Drive report upload was not completed: {}".format(exc), file=sys.stderr)
        return 1

if __name__ == "__main__":
    sys.exit(main())

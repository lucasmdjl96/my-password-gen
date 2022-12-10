# MyPasswordGen

## About this app

MyPasswordGen is a webapp for a generating and managing passwords that never transmits any password (not even
hashed) outside the device.

## Usage

MyPasswordGen works by generating unique passwords based on 4 factors: a master username and a master
password, both of which can be set in the log-in page, and an email (or username) and a site name for which
the password is to be used, both of which can be set in the page after logging-in.

The password generated is completely determined by these four factors, allowing the user to generate the same
password across different devices/sessions. Since the password is never stored anywhere, any password will
allow to proceed to the next page (usernames, email addresses and site names are linked to your session, so
no other user will be able to see this data), but the password that will be generated will be different for
each different master password input.

Once logged in, the user can save/select/delete email addresses for the username they have logged in with
and, after selecting one of these, they can save/select/delete site names for said email address, all of
which will remain available for successive log-ins.

All usernames the user registers are saved according to a session which is stored with a cookie on the
browser. So clearing the cookies will effectively eliminate all these usernames. To alleviate this, an
export/import functionality is offered to the user.

Clicking on the export button while on the log-in page will initiate the download of a file containing all
the data of the user's session, or will display a QR code with this data. This data can then be uploaded via
the Import button on the log-in page, which will allow the user to upload the previously downloaded file,
or scan a QR code. Note that importing a session will overwrite any previous data linked to the session.

Clicking on the export button while on the email/site page will do the same as in the login page, but just
with the data of the username currently logged in. Importing this data will not overwrite the data linked
to the session, but will fail if the username already exists.

Offline functionality is much the same except that no data is ever saved.

### Keyboard Shortcuts

ctrl + up / ctrl + down: can be used to move up or down among text input fields\
enter: can be used to advance to the next input, log in, add email/site and generate a password\
alt + enter: on the login page can be used to register a username\
ctrl + enter: on the login page can be used to toggle the online/offline mode switch\
ctrl + backspace: can be used to return to the login page from the email/site page\
ctrl + del: can be used to delete a saved email or site\
ctrl + e: can be used to export session/user data\
ctrl + i: can be used to import session/user data\
ctrl + q: can be used to toggle between export (or import) modes\
ctrl + c: can be used after generating a password to copy it to the clipboard

## Technical details

MyPasswordGen can work in two modes: Online mode and Offline mode; the chosen mode can be selected via the
toggle on the login page.

#### Online mode (Only available after accepting the cookie policy)

In this mode the client communicates with the server to remember some input data for future accesses. First,
a session id is created, stored in the database, and then stored as a cookie on the browser for a year and a
day (unless renewed).

Every user created is associated to this session id. All usernames, emails and sites created are sent to the
server and stored in the database after being hashed, while the password is only
stored in the browser's memory. When logging in or registering a user, the hash of the username is checked
against the database to verify if such a username is already associated to the session id.

After logging in, the email addresses inserted afterwards are linked to the username in the database, and
similarly the site names inserted are linked to the current email address. This allows the possibility of
recovering the list of emails and sites on future visits. Each successive login will have access to the data
stored previously, and the session id will be updated each time you access the login page with Online mode
active (and each time Online mode is toggled on). To recover the plain usernames, email addresses and site names the
indexedDB technology is used, linking them to the server-side ids.

Finally, the password is generated by hashing a combination of the username, master-password, email and site.
And therefore, the same combination of username, master-password, email and site will always yield the same
password. In this way a secure password can be generated consistently without the master password ever
leaving your device.

#### Offline mode

No communication whatsoever is held with the server in this mode. While the functionality is almost identical
to that of Online mode, everything is stored in your browser's memory, so no usernames, emails or sites will
be remembered for future log-ins. Additionally, in Offline mode changing the email address won't affect the
sites saved in-memory.

## License

This program is released under license GPLv3.

## WebSite

You can visit MyPasswordGen website [here](https://mypasswordgen.com).

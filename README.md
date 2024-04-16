[![Tech Doc](https://img.shields.io/badge/master-docs-875A7B.svg?style=flat&colorA=8F8F8F)](https://doubleclue.com/wp-content/uploads/DCEM_Manual_EN.pdf)
[![Help](https://img.shields.io/badge/master-help-875A7B.svg?style=flat&colorA=8F8F8F)](https://doubleclue.com/en/contact-us-eng/)

# [DoubleClue.com](https://www.doubleClue.com)

1. [Identity and Access Management]()
2. [PasswordSafe]()
3. [Modularity]()

# Identity and Access Management

DoubleClue is an Identity & Access Management (IAM) Software which facilitates the administration of identities as well as the management of access rights for various applications, systems and networks. A Multi-Factor Authentication (MFA) with eight different authentication methods.

## Installation and Download

DoubleClue can be installed on premisses (Windows or Linux 64Bit Systems). 
Alternatively you can start immediate using DoubleClue cloud.
To start testing the cloud version go to: 

#### Try the DoubleClue Cloud Version at

https://www.doubleclue.online/dcem/createTenant/index.xhtml

#### Download OnPremisses Version

Latest Release:
Windows: https://doubleclue.com/files/DCEM.zip
Linux: https://doubleclue.com/files/DCEM_Linux.tar.gz

For on premises installation please follow the link: <a href="https://doubleclue.com/wp-content/uploads/Quick_Installation_Guide_EN.pdf">Installation Guide</a>

## Interfaces

DoubleClue has build-in interfaces with the all the most popular Identity interfaces such as:

- RADIUS (mostly used by VPN, Firewalls and applications)
- SAML (Security Assertion Markup Language 2.0)
- OpenId (OAuth)
- REST-API

## Authentication

DoubleClue supports 8 different authentication methods.

- Push Approval: It uses the user's mobile phone, finger print or faces-id or password to do very comfortable MFA authentication. DoubleClue App is required
- SMS
- Voice Message
- Hardware OTP (One Time Password)
- Software OTP
- FIDO
- QR-Code Generator,  Requires DoubleClue App
- Password

## Integration

- Integrates with Microsoft Active Director
- Integrates with Microsoft Azure

# PasswordSafe

PasswordSafe the password management of DoubleClue. Your passwords are stored in (kdbx KeePass Format) files in DoubleClue CloudStorage 

- Password files are stored centrally. Never stored on mobile phones or workstations

- The Master-Key for the kdbx files is never stored in DoubleClue. 

- User can access passwords from UserPortal, DoubleClue App or by installing the DoubleClue Plug-in to KeePass.

- User can share kdbx files with other users or groups

# Modularity

DoubleClue is a very modular architecture. You can add plugin modules to DoubleClue. The module will have its own menus and actions.
For more information on how to create a new module see the document 'documentation/DevelopmentModule.odt'

## Menu and Action Privileges

Every module menu and action (button and links) will be managed by the administration privileges utility

## Auto GUI

AutoGUI is a utility which can list, display, edit and deletes objects with only some lines of code. 
For more information on how to create a new GUI see the document 'documentation/DevelopmentModule.odt'.

## Auto Preferences

This utility creates auto GUI for all module preferences. Developer needs just to add variable in the preferences Java class of the module

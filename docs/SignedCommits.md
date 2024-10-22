# Setting Up GPG Key for GitHub Commit Signing

## Step 1: Generate a GPG Key

1. Open your terminal.

2. Run the following command to generate a new GPG key, replacing `<your-email@example.com>` with your email address:

   ```bash
   gpg --gen-key
   ```
Follow the prompts to set up your GPG key, including choosing the key type and key length. Be sure to provide a secure passphrase when prompted.

## Step 2: List GPG Keys
After the key is generated, you can list your GPG keys by running:

```bash
gpg --list-keys
```
Find the GPG key ID, a long hexadecimal number, in the output.
eg :

```bash
pub   rsa3072 2023-12-30 [SC] [expires: 2025-12-29]
      E6F076E66343529CCE5519FEFF452087E382A621
uid           [ultimate] example <example@scis.smu.edu.sg>
sub   rsa3072 2023-12-30 [E] [expires: 2025-12-29]
```
The id is : `E6F076E66343529CCE5519FEFF452087E382A621`

## Step 3: Configure Git to Use GPG Key
Configure Git to use your GPG key for signing commits by running the following command, replacing <your-gpg-key-id> with the key ID from step 2:

```bash
git config --global user.signingkey <your-gpg-key-id>
```
## Step 4: Enable GPG Signing for Commits
Enable GPG signing for commits globally with:

```bash
git config --global commit.gpgsign true
```

## Step 5: Export Your GPG Public Key
Run the following command to export your GPG public key:

```bash
gpg --armor --export <your-email@example.com>
```
Copy the entire content of your GPG public key, starting with -----BEGIN PGP PUBLIC KEY BLOCK----- and ending with -----END PGP PUBLIC KEY BLOCK-----.

## Step 6: Add the GPG Public Key to GitHub
- Log in to your GitHub account.

- Click on your profile picture in the upper-right corner and select "Settings."

- In the left sidebar, click on "SSH and GPG keys."

- Under "GPG keys," click the "New GPG key" button.

- Paste the GPG public key you copied in step 5 into the "Key" field.

- Optionally, provide a name for the key in the "Title" field.

- Click the "Add GPG key" button.

- Confirm your GitHub password if prompted.


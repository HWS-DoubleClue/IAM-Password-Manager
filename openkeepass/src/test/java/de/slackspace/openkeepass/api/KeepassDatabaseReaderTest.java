package de.slackspace.openkeepass.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Attachment;
import de.slackspace.openkeepass.domain.CompressionAlgorithm;
import de.slackspace.openkeepass.domain.CrsAlgorithm;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassHeader;
import de.slackspace.openkeepass.domain.Property;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;
import de.slackspace.openkeepass.util.ByteUtils;
import de.slackspace.openkeepass.util.ResourceUtils;
import de.slackspace.openkeepass.util.StreamUtils;

public class KeepassDatabaseReaderTest {

    @Test
    public void whenGettingEntriesByTitleShouldReturnMatchingEntries() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("testDatabase.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcdefg");

        Entry entry = database.getEntryByTitle("MyEntry");
        Assert.assertEquals("1v4QKuIUT6HHRkbq0MPL", entry.getPassword());
    }

    @Test
    public void whenGettingModifiedEntriesByTitleShouldReturnMatchingEntries() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("testDatabaseModified.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcdefg");

        Entry entry = database.getEntryByTitle("MyEntry");
        Assert.assertEquals("1v4QKuIUT6HHRkbq0MPL", entry.getPassword());
    }

    @Test
    public void whenGettingEntriesByTitleButNothingMatchesShouldReturnNull() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("testDatabase.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcdefg");

        Entry entry = database.getEntryByTitle("abcdefgh");
        Assert.assertNull(entry);
    }

    @Test
    public void whenKeePassFileIsV2ShouldReadHeader() throws IOException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("testDatabase.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassHeader header = reader.getHeader();

        Assert.assertTrue(
                Arrays.equals(ByteUtils.hexStringToByteArray("31C1F2E6BF714350BE5805216AFC5AFF"), header.getCipher()));
        Assert.assertEquals(CompressionAlgorithm.Gzip, header.getCompression());
        Assert.assertEquals(8000, header.getTransformRounds());
        Assert.assertTrue("EncryptionIV is not 2c605455f181fbc9462aefb817852b37",
                Arrays.equals(ByteUtils.hexStringToByteArray("2c605455f181fbc9462aefb817852b37"),
                        header.getEncryptionIV()));
        Assert.assertTrue("StartBytes are not 69d788d9b01ea1facd1c0bf0187e7d74e4aa07b20d464f3d23d0b2dc2f059ff8", Arrays
                .equals(ByteUtils
                        .hexStringToByteArray("69d788d9b01ea1facd1c0bf0187e7d74e4aa07b20d464f3d23d0b2dc2f059ff8"),
                        header.getStreamStartBytes()));
        Assert.assertEquals(CrsAlgorithm.Salsa20, header.getCrsAlgorithm());
        Assert.assertTrue("MasterSeed is not 35ac8b529bc4f6e44194bccd0537fcb433a30bcb847e63156262c4df99c528ca",
                Arrays.equals(
                        ByteUtils.hexStringToByteArray(
                                "35ac8b529bc4f6e44194bccd0537fcb433a30bcb847e63156262c4df99c528ca"),
                        header.getMasterSeed()));
        Assert.assertTrue("TransformBytes are not 0d52d93efc5493ae6623f0d5d69bb76bd976bb717f4ee67abbe43528ebfbb646",
                Arrays.equals(
                        ByteUtils.hexStringToByteArray(
                                "0d52d93efc5493ae6623f0d5d69bb76bd976bb717f4ee67abbe43528ebfbb646"),
                        header.getTransformSeed()));
        Assert.assertTrue("ProtectedStreamKey is not ec77a2169769734c5d26e5341401f8d7b11052058f8455d314879075d0b7e257",
                Arrays
                        .equals(ByteUtils.hexStringToByteArray(
                                "ec77a2169769734c5d26e5341401f8d7b11052058f8455d314879075d0b7e257"),
                                header.getProtectedStreamKey()));
        Assert.assertEquals(210, header.getHeaderSize());
    }

    @Test
    public void whenPasswordIsValidShouldOpenKeepassFile() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("testDatabase.kdbx"));
        KeePassDatabase reader = KeePassDatabase.getInstance(file);

        KeePassFile database = reader.openDatabase("abcdefg");
        Assert.assertNotNull(database);

        Assert.assertEquals("TestDatabase", database.getMeta().getDatabaseName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void whenKeePassFileIsOldShouldThrowException() {
        byte[] header = ByteUtils.hexStringToByteArray("03d9a29a65fb4bb5");

        ByteArrayInputStream file = new ByteArrayInputStream(header, header.length, 0);
        KeePassDatabase.getInstance(file);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void whenNotAKeePassFileShouldThrowException() {
        byte[] header = ByteUtils.hexStringToByteArray("0011223344556677");

        ByteArrayInputStream file = new ByteArrayInputStream(header, header.length, 0);
        KeePassDatabase.getInstance(file);
    }

    @Test
    public void testIfPasswordsCanBeDecrypted() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("fullBlownDatabase.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("123456");

        List<Entry> entries = database.getEntries();

        Assert.assertEquals("2f29047129b9e4c48f05d09907e52b9b", entries.get(0).getPassword());
        Assert.assertEquals("GzteT206M4bVvHYaKPpA", entries.get(1).getPassword());
        Assert.assertEquals("gC03cizrzcBxytfKurWQ", entries.get(2).getPassword());
        Assert.assertEquals("jXjHEh3c8wcl0hank0qG", entries.get(3).getPassword());
        Assert.assertEquals("wkzB5KGIUoP8LKSSEngX", entries.get(4).getPassword());
    }

    @Test
    public void whenEntryHasCustomPropertiesShouldReadCustomProperties() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("fullBlownDatabase.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("123456");

        Entry entry = database.getEntryByTitle("6th Entry");

        Assert.assertEquals("6th Entry", entry.getTitle());
        Property customProperty = entry.getPropertyByName("x");

        Assert.assertNotNull("CustomProperty should not be null", customProperty);
        Assert.assertEquals("y", customProperty.getValue());
    }

    @Test
    public void whenPasswordOfEntryIsEmptyShouldReturnEmptyValue() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("DatabaseWithEmptyPassword.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("1234");

        Entry entryWithEmptyPassword = database.getEntryByTitle("EntryWithEmptyPassword");
        Assert.assertEquals("UsernameNotEmpty", entryWithEmptyPassword.getUsername());
        Assert.assertEquals("", entryWithEmptyPassword.getPassword());

        Entry entryWithEmptyUsername = database.getEntryByTitle("EntryWithEmptyUsername");
        Assert.assertEquals("", entryWithEmptyUsername.getUsername());
        Assert.assertEquals("1234", entryWithEmptyUsername.getPassword());

        Entry entryWithEmptyUserAndPassword = database.getEntryByTitle("EmptyEntry");
        Assert.assertEquals("", entryWithEmptyUserAndPassword.getUsername());
        Assert.assertEquals("", entryWithEmptyUserAndPassword.getPassword());
    }

    @Test
    public void whenKeePassFileIsSecuredWithBinaryKeyFileShouldOpenKeePassFileWithKeyFile()
            throws FileNotFoundException {
        FileInputStream keePassFile = new FileInputStream(ResourceUtils.getResource("DatabaseWithBinaryKeyfile.kdbx"));
        FileInputStream keyFile = new FileInputStream(ResourceUtils.getResource("0.png"));

        KeePassFile database = KeePassDatabase.getInstance(keePassFile).openDatabase(keyFile);

        List<Entry> entries = database.getEntries();
        Assert.assertEquals("1234567", entries.get(0).getPassword());
    }

    @Test
    public void whenKeePassFileIsSecuredWithBinaryKeyFileAndPasswordShouldOpenKeePassFile()
            throws FileNotFoundException {
        FileInputStream keePassFile =
                new FileInputStream(ResourceUtils.getResource("DatabaseWithPasswordAndBinaryKeyfile.kdbx"));
        FileInputStream keyFile = new FileInputStream(ResourceUtils.getResource("0.png"));

        KeePassFile database = KeePassDatabase.getInstance(keePassFile).openDatabase("1234", keyFile);

        List<Entry> entries = database.getEntries();
        Assert.assertEquals("qwerty", entries.get(0).getPassword());
    }

    @Test
    public void whenKeePassFileIsSecuredWithKeyFileShouldOpenKeePassFileWithKeyFile() throws FileNotFoundException {
        FileInputStream keePassFile = new FileInputStream(ResourceUtils.getResource("DatabaseWithKeyfile.kdbx"));
        FileInputStream keyFile = new FileInputStream(ResourceUtils.getResource("DatabaseWithKeyfile.key"));

        KeePassFile database = KeePassDatabase.getInstance(keePassFile).openDatabase(keyFile);

        List<Entry> entries = database.getEntries();
        Assert.assertEquals("V6uoqOm7esGRqm20VvMz", entries.get(0).getPassword());
    }

    @Test
    public void whenKeePassFileIsSecuredWithPasswordAndKeyFileShouldOpenKeePassFileWithPasswordAndKeyFile()
            throws FileNotFoundException {
        FileInputStream keePassFile =
                new FileInputStream(ResourceUtils.getResource("DatabaseWithPasswordAndKeyfile.kdbx"));
        FileInputStream keyFile = new FileInputStream(ResourceUtils.getResource("DatabaseWithPasswordAndKeyfile.key"));

        KeePassFile database = KeePassDatabase.getInstance(keePassFile).openDatabase("test123", keyFile);

        List<Entry> entries = database.getEntries();
        Assert.assertEquals("V6uoqOm7esGRqm20VvMz", entries.get(0).getPassword());
    }

    @Test(expected = KeePassDatabaseUnreadableException.class)
    public void whenKeePassFileIsSecuredWithPasswordAndKeyFileShouldNotOpenKeePassFileWithPassword()
            throws FileNotFoundException {
        FileInputStream keePassFile =
                new FileInputStream(ResourceUtils.getResource("DatabaseWithPasswordAndKeyfile.kdbx"));

        KeePassDatabase.getInstance(keePassFile).openDatabase("test123");
    }

    @Test(expected = KeePassDatabaseUnreadableException.class)
    public void whenKeePassFileIsSecuredWithPasswordAndKeyFileShouldNotOpenKeePassFileWithKeyFile()
            throws FileNotFoundException {
        FileInputStream keePassFile =
                new FileInputStream(ResourceUtils.getResource("DatabaseWithPasswordAndKeyfile.kdbx"));
        FileInputStream keyFile = new FileInputStream(ResourceUtils.getResource("DatabaseWithPasswordAndKeyfile.key"));

        KeePassDatabase.getInstance(keePassFile).openDatabase(keyFile);
    }

    @Test
    public void whenGettingInstanceByStringShouldOpenDatabase() throws FileNotFoundException {
        KeePassFile database =
                KeePassDatabase.getInstance(ResourceUtils.getResource("fullBlownDatabase.kdbx")).openDatabase("123456");
        List<Entry> entries = database.getEntries();
        Assert.assertEquals("2f29047129b9e4c48f05d09907e52b9b", entries.get(0).getPassword());
    }

    @Test
    public void whenGettingInstanceByFileShouldOpenDatabase() {
        KeePassFile database = KeePassDatabase
                .getInstance(new File(ResourceUtils.getResource("fullBlownDatabase.kdbx"))).openDatabase("123456");
        List<Entry> entries = database.getEntries();
        Assert.assertEquals("2f29047129b9e4c48f05d09907e52b9b", entries.get(0).getPassword());
    }

    @Test
    public void whenGettingEntriesFromKeeFoxShouldDecryptEntries() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("KeeFoxDatabase.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcd1234");

        List<Entry> entries = database.getEntries();
        Assert.assertEquals("Sample Entry", entries.get(0).getTitle());
        Assert.assertEquals("Password", entries.get(0).getPassword());
        Assert.assertEquals("Sample Entry #2", entries.get(1).getTitle());
        Assert.assertEquals("12345", entries.get(1).getPassword());
        Assert.assertEquals("Sign in - Google Accounts", entries.get(2).getTitle());
        Assert.assertEquals("test", entries.get(2).getPassword());
    }

    @Test
    public void whenGettingEntryByUUIDShouldReturnFoundEntry() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("testDatabase.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcdefg");

        Entry entry = database.getEntryByUUID(UUID.fromString("1fbddfcd-52ff-1d4b-b2e8-27f671e4ea22"));
        Assert.assertEquals("Sample Entry #2", entry.getTitle());
    }

    @Test
    public void whenGettingGroupByUUIDShouldReturnFoundGroup() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("testDatabase.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcdefg");

        Group group = database.getGroupByUUID(UUID.fromString("16abcc27-cca3-9544-8012-df4e98d4a3d8"));
        Assert.assertEquals("General", group.getName());
    }

    @Test
    public void whenGettingEntriesShouldReturnAllEntries() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("DatabaseWithComplexTree.kdbx"));

        KeePassFile db = KeePassDatabase.getInstance(file).openDatabase("MasterPassword");

        Assert.assertEquals(122, db.getEntries().size());
    }

    @Test
    public void whenGettingTagsShouldReturnTags() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("DatabaseWithTags.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcdefg");

        Entry entry = database.getEntryByTitle("Sample Entry");
        List<String> tags = entry.getTags();

        assertThat(tags, hasItems("tag1", "tag2", "tag3"));
    }

    @Test
    public void whenGettingColorsShouldReturnColors() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("DatabaseWithColors.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("qwerty");

        Entry entry = database.getEntryByTitle("Sample Entry");

        Assert.assertEquals("#0080FF", entry.getForegroundColor());
        Assert.assertEquals("#FF0000", entry.getBackgroundColor());
    }

    @Test
    public void whenGettingAttachmentShouldReturnAttachment() throws IOException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("DatabaseWithAttachments.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcdefg");

        Entry entry = database.getEntryByTitle("Sample Entry");

        Assert.assertEquals(2, entry.getAttachments().size());
        List<Attachment> attachments = entry.getAttachments();

        Attachment image = attachments.get(0);
        assertThat(image.getKey(), is("0.png"));
        assertThat(image.getRef(), is(0));

        FileInputStream originalImage = new FileInputStream(ResourceUtils.getResource("0.png"));
        byte[] originalByteArray = StreamUtils.toByteArray(originalImage);

        assertThat(image.getData(), equalTo(originalByteArray));
    }

    @Test
    public void whenGettingEntryWithReferencesShouldReturnReferencedValues() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("DatabaseWithReferences.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("abcdefg");

        Entry entryA = database.getEntryByTitle("ReferenceToA");
        assertThat(entryA.getUsername(), is("testA"));
        assertThat(entryA.getPassword(), is("passwdA"));
        assertThat(entryA.getUrl(), is("http://google.com"));
        assertThat(entryA.getNotes(), is("Just a sample note"));

        Entry entryB = database.getEntryByTitle("AnotherReferenceToA");
        assertThat(entryB.getUsername(), is("passwdA"));
        assertThat(entryB.getPassword(), is("http://google.com"));
    }

    @Test
    public void whenReadingDatabaseFromKeeWebShouldDecryptPasswordsCorrectly() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("DatabaseFromKeeWeb.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("demo");

        Entry entryA = database.getEntryByTitle("item");
        assertThat(entryA.getUsername(), is("user"));
        assertThat(entryA.getPassword(), is("password"));

        Property customerProperty = entryA.getCustomProperties().get(0);
        assertThat(customerProperty.getKey(), is("New Field"));
        assertThat(customerProperty.getValue(), is("newpasswordfield"));

        Entry entryB = database.getEntryByTitle("entry");
        assertThat(entryB.getUsername(), is("erwerwe"));
        assertThat(entryB.getPassword(), is("werwer"));
    }

    @Test
    public void whenReadingDatabaseFromKeepassDroidShouldIgnoreMissingProperties() throws FileNotFoundException {
        FileInputStream file = new FileInputStream(ResourceUtils.getResource("DatabaseFromKeepassDroid.kdbx"));

        KeePassDatabase reader = KeePassDatabase.getInstance(file);
        KeePassFile database = reader.openDatabase("1");

        Entry entry = database.getEntryByTitle("entry1");
        assertThat(entry.getUsername(), is("aga"));
        assertThat(entry.getPassword(), is("secret"));
    }
}

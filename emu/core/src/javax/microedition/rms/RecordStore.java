package javax.microedition.rms;

import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.EmuHost;

/**
 * Emulator implementation of the RMS RecordStore. Records live in memory and
 * are written through to the host backend on every change. Record ids start
 * at 1 as in MIDP. Enumeration/filters are not emulated.
 */
public class RecordStore {
    private static final Hashtable OPEN = new Hashtable();

    private final String name;
    private final Vector records = new Vector(); // index = id-1, null = deleted
    private int openCount;

    private RecordStore(String name) {
        this.name = name;
    }

    public static RecordStore openRecordStore(String name, boolean create)
            throws RecordStoreException, RecordStoreFullException, RecordStoreNotFoundException {
        if (name == null || name.length() == 0 || name.length() > 32) throw new IllegalArgumentException();
        RecordStore rs = (RecordStore) OPEN.get(name);
        if (rs == null) {
            byte[][] data = EmuHost.backend == null ? null : EmuHost.backend.loadStore(name);
            if (data == null && !create) throw new RecordStoreNotFoundException(name);
            rs = new RecordStore(name);
            if (data != null) {
                for (int i = 0; i < data.length; i++) rs.records.addElement(data[i]);
            } else if (EmuHost.backend != null) {
                EmuHost.backend.saveStore(name, new byte[0][]);
            }
            OPEN.put(name, rs);
        }
        rs.openCount++;
        return rs;
    }

    public static void deleteRecordStore(String name) throws RecordStoreException, RecordStoreNotFoundException {
        RecordStore rs = (RecordStore) OPEN.get(name);
        if (rs != null && rs.openCount > 0) throw new RecordStoreException("store is open");
        OPEN.remove(name);
        if (EmuHost.backend != null) {
            if (EmuHost.backend.loadStore(name) == null) throw new RecordStoreNotFoundException(name);
            EmuHost.backend.deleteStore(name);
        }
    }

    public static String[] listRecordStores() {
        return null;
    }

    public void closeRecordStore() throws RecordStoreNotOpenException, RecordStoreException {
        check();
        openCount--;
    }

    public String getName() { return name; }

    public int getNumRecords() throws RecordStoreNotOpenException {
        check();
        int n = 0;
        for (int i = 0; i < records.size(); i++) if (records.elementAt(i) != null) n++;
        return n;
    }

    public int getNextRecordID() throws RecordStoreNotOpenException {
        check();
        return records.size() + 1;
    }

    public int addRecord(byte[] data, int offset, int len) throws RecordStoreNotOpenException, RecordStoreException {
        check();
        byte[] copy = new byte[len];
        if (len > 0) System.arraycopy(data, offset, copy, 0, len);
        records.addElement(copy);
        flush();
        return records.size();
    }

    public void setRecord(int id, byte[] data, int offset, int len) throws RecordStoreException {
        check();
        valid(id);
        byte[] copy = new byte[len];
        if (len > 0) System.arraycopy(data, offset, copy, 0, len);
        records.setElementAt(copy, id - 1);
        flush();
    }

    public void deleteRecord(int id) throws RecordStoreException {
        check();
        valid(id);
        records.setElementAt(null, id - 1);
        flush();
    }

    public byte[] getRecord(int id) throws RecordStoreException {
        check();
        valid(id);
        byte[] r = (byte[]) records.elementAt(id - 1);
        byte[] copy = new byte[r.length];
        System.arraycopy(r, 0, copy, 0, r.length);
        return copy.length == 0 ? null : copy;
    }

    public int getRecord(int id, byte[] buffer, int offset) throws RecordStoreException {
        check();
        valid(id);
        byte[] r = (byte[]) records.elementAt(id - 1);
        System.arraycopy(r, 0, buffer, offset, r.length);
        return r.length;
    }

    public int getRecordSize(int id) throws RecordStoreException {
        check();
        valid(id);
        return ((byte[]) records.elementAt(id - 1)).length;
    }

    public int getSize() { return 1024; }
    public int getSizeAvailable() { return 65536; }
    public int getVersion() { return 1; }
    public long getLastModified() { return 0; }

    private void check() throws RecordStoreNotOpenException {
        if (openCount <= 0) throw new RecordStoreNotOpenException(name);
    }

    private void valid(int id) throws InvalidRecordIDException {
        if (id < 1 || id > records.size() || records.elementAt(id - 1) == null) throw new InvalidRecordIDException();
    }

    private void flush() {
        if (EmuHost.backend == null) return;
        byte[][] out = new byte[records.size()][];
        for (int i = 0; i < out.length; i++) out[i] = (byte[]) records.elementAt(i);
        EmuHost.backend.saveStore(name, out);
    }
}

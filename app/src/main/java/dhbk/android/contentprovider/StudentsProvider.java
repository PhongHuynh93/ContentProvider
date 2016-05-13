package dhbk.android.contentprovider;

/**
 * Created by huynhducthanhphong on 4/26/16.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.HashMap;

public class StudentsProvider extends ContentProvider {
    // TODO: 5/13/16 bước 2: tạo uri cho content provider dạng <prefix>://<authority>/<data_type>/<id>
    static final String PROVIDER_NAME = "com.example.provider.College";
    static final String URL = "content://" + PROVIDER_NAME + "/students";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String NAME = "name";
    static final String GRADE = "grade";

    private static HashMap<String, String> STUDENTS_PROJECTION_MAP;

    // TODO: 5/13/16 bước 7: trong từng query mà ta gửi đến content provider, phải gán cho nó 1 số int để xác định
    // tạo 2 dạng int: dạng int cho table và dạng int cho tưng row
    private static final int STUDENTS = 1;
    private static final int STUDENT_ID = 2;

    // TODO: 5/13/16 bước 9: match từng uri cho số int
    private static final UriMatcher uriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(PROVIDER_NAME, "students", STUDENTS); // match table.
        matcher.addURI(PROVIDER_NAME, "students/#", STUDENT_ID); // match each row.
        return matcher;
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;


    // TODO: 5/13/16 bước 8 :  trong onCreate của content provider , ta gọi hàm tạo database
    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    // TODO: 5/13/16 bước 10 hiện thực hàm getType() để trả về đúng loại MIME của kết quả
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            /**
             * Get all student records
             */
            case STUDENTS:
                return "vnd.android.cursor.dir/vnd.example.students";

            /**
             * Get a particular student
             */
            case STUDENT_ID:
                return "vnd.android.cursor.item/vnd.example.students";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    // TODO: 5/13/16 bươc 11 hiện thực hàm query() để lấy giá trị của 1 table trong databse thích hợp với từng loại yêu cầu của ta (liệt kê hết) khi dc client yêu cầu
    @Override
    // uri tham số là uri mà từ app khác gọi tới
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // tạo query để send đến database
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        // mặc dịnh query table tên là vậy luôn
        qb.setTables(DatabaseHelper.STUDENTS_TABLE_NAME);

        // so sánh uri tham số với uri của content provider xem xó trùng ko
        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                qb.setProjectionMap(STUDENTS_PROJECTION_MAP);
                break;

            case STUDENT_ID:
                // thêm tham số là student ID vào khung query
                qb.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == "") {
            /**
             * By default sort on student names
             */
            sortOrder = NAME;
        }

        // return kết quả đáp ứng query
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    // TODO: 5/13/16 bước 12: insert 1 record dạng ContentValue vào table
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        /**
         * Add a new student record
         */
        long rowID = db.insert(DatabaseHelper.STUDENTS_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */

        // add rồi thì thông báo là content provider này đã dc thay đổi data -> để các app mà có sd content provider này biết
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    // TODO: 5/13/16 hiện thực update / delete content provider
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                count = db.delete(DatabaseHelper.STUDENTS_TABLE_NAME, selection, selectionArgs);
                break;

            case STUDENT_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(DatabaseHelper.STUDENTS_TABLE_NAME, _ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case STUDENTS:
                count = db.update(DatabaseHelper.STUDENTS_TABLE_NAME, values, selection, selectionArgs);
                break;

            case STUDENT_ID:
                count = db.update(DatabaseHelper.STUDENTS_TABLE_NAME, values, _ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}

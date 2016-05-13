package dhbk.android.contentprovider;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void onClickAddName(View view) {
        // Add a new student record
        ContentValues values = new ContentValues();

        // 2 column +  2 value
        values.put(StudentsProvider.NAME,
                ((EditText) findViewById(R.id.editText2)).getText().toString());
        values.put(StudentsProvider.GRADE,
                ((EditText) findViewById(R.id.editText3)).getText().toString());

        // access to content provider by call method of ContentResolver
        // Inserts a row into a table at the given URL.
        Uri uri = getContentResolver().insert(
                StudentsProvider.CONTENT_URI, values);

        Toast.makeText(getBaseContext(),
                uri.toString(), Toast.LENGTH_LONG).show();
    }

    public void onClickRetrieveStudents(View view) {
        // Retrieve student records
        String URL = "content://com.example.provider.College/students";
        Uri students = Uri.parse(URL);

        // TODO: 5/13/16 bước 15: cach lấy data trong content provider (do ta chỉ match table tên "students" nên c return toàn table)
        // c chứa record dược sort theo "name" trong database
        Cursor c = getContentResolver().query(students, null, null, null, "name");

        StringBuilder listTableStudent = new StringBuilder();

        if (c != null && c.moveToFirst()) {
            do {
                // TODO: 5/13/16 replace with TextView
//                Toast.makeText(this,
//                        c.getString(c.getColumnIndex(StudentsProvider._ID)) +
//                                ", " + c.getString(c.getColumnIndex(StudentsProvider.NAME)) +
//                                ", " + c.getString(c.getColumnIndex(StudentsProvider.GRADE)),
//                        Toast.LENGTH_SHORT).show();
                listTableStudent
                        .append(c.getString(c.getColumnIndex(StudentsProvider._ID)))
                        .append(", ")
                        .append(c.getString(c.getColumnIndex(StudentsProvider.NAME)))
                        .append(", ").append(c.getString(c.getColumnIndex(StudentsProvider.GRADE)))
                        .append("\n");
            } while (c.moveToNext());

            // TODO: 5/13/16 liệt kê những dữ liêu co trong cursor.
            TextView listStudentTV = (TextView) findViewById(R.id.list_student);
            listStudentTV.setText(listTableStudent.toString());
            c.close();
        }
    }
}
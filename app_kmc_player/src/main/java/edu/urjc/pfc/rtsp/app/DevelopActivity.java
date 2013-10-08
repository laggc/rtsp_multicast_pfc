package edu.urjc.pfc.rtsp.app;

import edu.urjc.pfc.rtsp.pruebas.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class DevelopActivity extends Activity {

	public final static String LOG_TAG = "DevelopActivity";

	private EditText editTextUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_develop);



		editTextUri = (EditText)findViewById(R.id.editTextUri);
	}

	public void lanzarPlayer(View view) {

		try {
			String uriString = editTextUri.getText().toString();

			if(uriString == null || uriString.equals("")) {
				throw new Exception("Incorrect Uri");
			}

			Uri data = Uri.parse(uriString);

			Intent intent = new Intent(this, PlayerActivity.class);

			intent.setData(data);

			startActivity(intent);

		}catch(Exception e) {
			Toast.makeText(DevelopActivity.this, "URI is null", Toast.LENGTH_SHORT).show();
		}
	}
}

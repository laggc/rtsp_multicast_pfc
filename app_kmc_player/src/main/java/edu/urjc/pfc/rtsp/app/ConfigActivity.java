package edu.urjc.pfc.rtsp.app;
 
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.urjc.pfc.rtsp.pruebas.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class ConfigActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new OpcionesFragment()).commit();
	}

	public static class OpcionesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

		private String initial_ip;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.opciones);

			//Itero sobre todas la propiedades para inicializar el summary
			Map<String, ?> pref = getPreferenceManager().getSharedPreferences().getAll();
			for(Map.Entry<String, ?> entry: pref.entrySet()) {
				onSharedPreferenceChanged(null, entry.getKey());

				//Guardo la IP que tengo al principio
				if(entry.getKey().equals(getActivity().getResources().getString(R.string.preference_ip_key))) {
					initial_ip = entry.getValue().toString();
				}
			}
		}

		@Override
		public void onResume() {
			super.onResume();

			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();

			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

			Preference pref = findPreference(key);

			if ((pref != null) && (pref instanceof EditTextPreference)) {
				EditTextPreference etp = (EditTextPreference) pref;

				if(key.equals(getActivity().getResources().getString(R.string.preference_ip_key))) {

					if(!validateIP(etp.getText())) {

						final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("Formato IP Incorrecto");
						builder.setMessage("Introduzca una dirección IP en formato correcto.");
						builder.setPositiveButton(android.R.string.ok, null);
						builder.show();

						etp.setText(initial_ip);

					}else {
						initial_ip = etp.getText();
					}
				}
				pref.setSummary(etp.getText());
			}
			
			if ((pref != null) && (pref instanceof ListPreference)) {
				ListPreference etp = (ListPreference) pref;
				pref.setSummary(etp.getValue());
			}
		}

		private static final String PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + 
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

		public static boolean validateIP(final String ip){          

			Pattern pattern = Pattern.compile(PATTERN);
			Matcher matcher = pattern.matcher(ip);
			return matcher.matches();             
		}
	}
}




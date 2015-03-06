package motocitizen.app.mc;

import motocitizen.main.R;
import motocitizen.startup.Startup;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MCObjects {
	public static final Button authButton = (Button) findView(R.id.app_mcaccidents_register_button);
	public static final Button authConfirmButton = (Button) findView(R.id.mc_auth_confirm_button);
	public static final Button authCancelButton = (Button) findView(R.id.mc_auth_cancel_button);
	public static final Button newMessageButton = (Button) findView(R.id.mc_new_message_send);
	public static final ImageButton dialButton = (ImageButton) findView(R.id.dial_button);
	public static final ImageButton createAccButton = (ImageButton) findView(R.id.mc_add_point_button);
	public static final CheckBox authAnonimCheckBox = (CheckBox) findView(R.id.mc_auth_anonim);
	public static final Button firstLoginButton = (Button) findView(R.id.first_auth_login);
	public static final Button anonimButton = (Button) findView(R.id.first_auth_anonim);
	public static final Button selectSoundButton = (Button) findView(R.id.mc_notif_sound_button);
	public static final Button selectSoundConfirmButton = (Button) findView(R.id.select_sound_save_button);
	public static final Button selectSoundCancelButton = (Button) findView(R.id.select_sound_cancel_button);
	public static final EditText inputCommentField = (EditText) findView(R.id.mc_new_message_text);
	public static final Button onwayButton = (Button) findView(R.id.onway_button);
	public static final View onwayContent = findView(R.id.acc_volunteers); 
	/*
	 * Описание группы основных закладок
	 */
	public static final RadioGroup mainTabsGroup = (RadioGroup) findView(R.id.main_tabs_group);
	public static final RadioButton tabAccidentsButton = (RadioButton) findView(R.id.tab_accidents_button);
	public static final RadioButton tabDetailsButton = (RadioButton) findView(R.id.tab_acc_details_button);
	public static final RadioButton tabMapButton = (RadioButton) findView(R.id.tab_map_button);
	public static final View accListView = findView(R.id.mc_acc_list);
	public static final View accDetailsView = findView(R.id.mc_acc_details);
	public static final View osmMapContainer = findView(R.id.osm_map_container);
	/*
	 * Описание группы закладок внутри деталей происшествия
	 */
	public static final RadioGroup mcDetTabsGroup = (RadioGroup) findView(R.id.mc_det_tabs_group);
	public static final RadioButton mcDetTabMessages = (RadioButton) findView(R.id.mc_det_tab_messages);
	public static final RadioButton mcDetTabHistory = (RadioButton) findView(R.id.mc_det_tab_history);
	public static final RadioButton mcDetTabPeople = (RadioButton) findView(R.id.mc_det_tab_people);
	public static final View detMessages = findView(R.id.det_messages);
	public static final View detHistory = findView(R.id.det_history);
	public static final View detVolunteers = findView(R.id.det_volunteers);
	
	private static View findView(int id) {
		return ((Activity) Startup.context).findViewById(id);
	}
}
package com.publictransportation.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.publictransportation.R;
import com.publictransportation.R.layout;
import com.publictransportation.utils.ServerCommunication;

/**
 * Activity to show route details to user. This activity receive selectedRouteId, 
 * selectedRouteShortName and selectedRouteLongName from ListSearch.
 * 
 * @author Henio
 * @since 2014/02
 */
public class RouteDetail extends Activity {
	
	private static final String URL_FIND_DEPARTURES_BY_ROUTE_ID = "findDeparturesByRouteId/run";
	private static final String WEEKDAY = "WEEKDAY";
	private static final String SATURDAY = "SATURDAY";
	private String selectedRouteId;
	private String selectedRouteShortName;
	private String selectedRouteLongName;
	private JSONObject routeDetailsJson;
	private List<String> timeTableWeekday;
	private List<String> timeTableSaturday;
	private List<String> timeTableSunday;
	
	private TextView routeName;
	private TextView noTimetable;
	private TextView loading;
	private TextView selectDayOption;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_detail);
		
		Log.i("Creating activity", "RouteDetail");
		
		this.selectDayOption = (TextView) findViewById(R.id.select_day_option);
		this.loading = (TextView) findViewById(R.id.loadingDetail);
		this.noTimetable = (TextView) findViewById(R.id.noTimetable);
		this.routeName = (TextView) findViewById(R.id.route_name);
		this.getSelectedRouteDetails();
		this.routeName.setText(this.selectedRouteShortName + " - " + this.selectedRouteLongName);
		this.findRouteDetailsById();
	}
	
	/**
	 * Get selectedRouteId, selectedRouteShortName and selectedRouteLongName from 
	 * intend bundle
	 * 
	 * @return selectedRouteId
	 */
	private void getSelectedRouteDetails(){
		Bundle bundle = getIntent().getExtras();
		this.selectedRouteId = bundle.getString("selectedRouteId");
		this.selectedRouteShortName = bundle.getString("selectedRouteShortName");
		this.selectedRouteLongName = bundle.getString("selectedRouteLongName");
	}
	
	/**
	 * Show interface details after loading data
	 */
	private void populateInterface(){
		this.loading.setVisibility(View.GONE);
		this.selectDayOption.setVisibility(View.VISIBLE);
		this.populateButtons();
	}
	
	/**
	 * Select click event on buttons and show all
	 */
	private void populateButtons(){
		Button weekdayButton = (Button) findViewById(R.id.weekdayButton);
		Button saturdayButton = (Button) findViewById(R.id.saturdayButton);
		Button sundayButton = (Button) findViewById(R.id.sundayButton);
		
		View.OnClickListener onClickListener = new EventShowTimetableOfDay();
		weekdayButton.setOnClickListener(onClickListener);
		saturdayButton.setOnClickListener(onClickListener);
		sundayButton.setOnClickListener(onClickListener);

		RelativeLayout buttonsLayout = (RelativeLayout) findViewById(R.id.buttonsLayout);
		buttonsLayout.setVisibility(View.VISIBLE);
		
		Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setVisibility(View.VISIBLE);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RouteDetail.this.finish();
			}
		});
	}
	
	/**
	 * Get route details by id and populate on interface
	 */
	private void findRouteDetailsById(){
		AccessServerDetails accessServer = new AccessServerDetails();
		accessServer.execute();
	}
	
	/**
	 * Get timetable from selected day and populate on interface
	 * 
	 * @param day
	 */
	private void populateTimetableOfDay(final String day){
		Log.i("Selected timetable", day);
		
		List<String> listToPopulate = new ArrayList<String>();
		if(RouteDetail.WEEKDAY.equalsIgnoreCase(day)){
			if(this.timeTableWeekday == null){
				this.timeTableWeekday = this.getSelectedTimetable(day);
			}
			listToPopulate = this.timeTableWeekday;
		} else if(RouteDetail.SATURDAY.equalsIgnoreCase(day)){
			if(this.timeTableSaturday == null){
				this.timeTableSaturday = this.getSelectedTimetable(day);
			}
			listToPopulate = this.timeTableSaturday;
		} else {
			// SUNDAY
			if(this.timeTableSunday == null){
				this.timeTableSunday = this.getSelectedTimetable(day);
			} 
			listToPopulate = this.timeTableSunday;
		}
		
		ListView timetableList = (ListView) findViewById(R.id.timetableList);
		if(listToPopulate.size() > 0){
			this.noTimetable.setVisibility(View.GONE);
			timetableList.setVisibility(View.VISIBLE);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, layout.layout_list_details, listToPopulate);
			timetableList.setAdapter(adapter);
		} else {
			timetableList.setVisibility(View.GONE);
			this.noTimetable.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Use selected day (Weekday, Saturday or Sunday) to get correct times
	 * 
	 * @param day
	 * @return timeTable
	 */
	private List<String> getSelectedTimetable(final String day){
		List<String> timeTable = new ArrayList<String>();
		try {
			JSONArray jsonArray = this.routeDetailsJson.getJSONArray("rows");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				if(day.equalsIgnoreCase(object.getString("calendar"))){
					timeTable.add(object.getString("time"));
				}
			}
		} catch (JSONException e) {
			// If jsonArray hasn't rows, then there is error on server communication (considered internet connection error)
			Log.e("Error to get times", e.toString());
			ProgressDialog.show(RouteDetail.this, "Error", getString(R.string.internet_connection_error), false, true);
		}
		return timeTable;
	}
	
	/**
	 *  Inner class to click events of day buttons (Weekday, Saturday and Sunday)
	 *  
	 *  @author Henio
	 *  @since 2014/02
	 */
	private class EventShowTimetableOfDay implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			Button button = (Button) v;
			RouteDetail.this.populateTimetableOfDay(button.getText().toString());
		}
	}
	
	/**
	 * Execute in background. Access server to order route details. 
	 * This asynchronous task allow maintain a responsive user interface and publish progress.
	 * 
	 * @author Henio
	 * @since 2014/02
	 */
	private class AccessServerDetails extends AsyncTask<JSONObject, String, JSONObject>{

		/**
		 * Executed before server communication. Show loading message to user
		 */
		@Override
		protected void onPreExecute() {
			RouteDetail.this.loading.setVisibility(View.VISIBLE);
		}
		
		/**
		 * Access server to order route details
		 */
		@Override
		protected JSONObject doInBackground(JSONObject... params) {
			final JSONObject routeDetailJson = this.buildJsonDetails();
			JSONObject result = ServerCommunication.postData(RouteDetail.URL_FIND_DEPARTURES_BY_ROUTE_ID, routeDetailJson);
			return result;
		}
		
		/**
		 * Executed after server communication.
		 * Returnto RouteDetail to populate details on interface
		 */
		@Override
		protected void onPostExecute(JSONObject result) {
			RouteDetail.this.routeDetailsJson = result;
			RouteDetail.this.populateInterface();
		}
		
		/**
		 * Build a JSONObject to get route details by id on server
		 * 
		 * JSON format:
		 * 
		 * {"params":
		 * 		{
		 *  		"routeId": 17
		 *  	}
		 *  }
		 */
		private JSONObject buildJsonDetails(){
			JSONObject paramsJson = new JSONObject();
			try {
				JSONObject routeIdJson = new JSONObject();
				routeIdJson.put("routeId", RouteDetail.this.selectedRouteId);
				paramsJson.put("params", routeIdJson);
			} catch (JSONException e) {
				Log.e("Error to build routeIdJson", e.toString());
			}
			return paramsJson;
		}
	}
}

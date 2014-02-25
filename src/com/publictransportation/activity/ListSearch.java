package com.publictransportation.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.publictransportation.R;
import com.publictransportation.R.layout;
import com.publictransportation.model.Route;
import com.publictransportation.model.RoutesArrayAdapter;
import com.publictransportation.utils.ServerCommunication;

/**
 * Launcher activity. Offers a EditText for user to insert an street name and click search. When clicked, 
 * this activity call ServerCommunication.java to get routes to typed street and show as a list.
 * 
 * @author Henio
 * @since 2014/02
 */
public class ListSearch extends Activity {
	
	private static final String URL_FIND_ROUTES = "findRoutesByStopName/run";
	
	private EditText searchBox;
	private TextView noRouteFound;
	private TextView loading;
	ListView listOfRoutesView;
	private List<Route> listOfRoutesToShow;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_search);
		
		Log.i("Creating activity", "ListSearch");
		
		this.searchBox = (EditText) findViewById(R.id.searchBox);
		this.noRouteFound = (TextView) findViewById(R.id.noRouteFound);
		this.loading = (TextView) findViewById(R.id.loadingList);
		this.listOfRoutesView = (ListView) findViewById(R.id.listOfRoutes);
		
		this.populateButtons();
	}
	
	/**
	 * Select click events to buttons. This activity has only one button.
	 */
	private void populateButtons(){
		Button searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(ListSearch.this.validateStreetNameToSearch()){
					ListSearch.this.hideVirtualKeyboard(ListSearch.this.getCurrentFocus().getWindowToken());
					ListSearch.this.findRoutesByStopName();
				} else {
					// hide all messagens and list
					ListSearch.this.loading.setVisibility(View.GONE);
					ListSearch.this.noRouteFound.setVisibility(View.GONE);
					ListSearch.this.listOfRoutesView.setVisibility(View.GONE);
				}
			}
		});
	}

	/**
	 * Checks if street name is a not null value
	 * 
	 * @return streetNameOk
	 */
	private boolean validateStreetNameToSearch(){
		boolean streetNameOk = true;
		if(this.searchBox.getText() == null || "".equals(this.searchBox.getText().toString())){
			this.searchBox.setError(getString(R.string.error_enter_street_name));
			streetNameOk = false;
		} else {
			this.searchBox.setError(null);
		}
		return streetNameOk;
	}
	

	/**
	 * Call an asynchronous task to get list of routes by street name and populate on interface
	 */
	private void findRoutesByStopName(){
		JSONObject paramsJson = this.buildJsonListRoutesByStreetName();
		
		FetchServerRoutes accessServer = new FetchServerRoutes();
		accessServer.execute(paramsJson);
	}
	
	/**
	 * Build a JSONObject to get routes by street name on server
	 * 
	 * JSON format:
	 * 
	 * {"params":
	 * 		{
	 *  		"stopName": "%lauro linhares%"
	 *  	}
	 *  }
	 */
	private JSONObject buildJsonListRoutesByStreetName(){
		JSONObject paramsJson = new JSONObject();
		try {
			JSONObject stopNameJson = new JSONObject();
			stopNameJson.put("stopName", "%" + this.searchBox.getText().toString() + "%");
			
			Log.i("Street value", this.searchBox.getText().toString());
			
			paramsJson.put("params", stopNameJson);
		} catch (JSONException e) {
			Log.e("Error to build stopNameJson", e.toString());
		}
		return paramsJson;
	}
	
	
	public void hideVirtualKeyboard(final IBinder token){
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(token, 0);
	}
	
	/**
	 * Populate a list to show to user and save an ArrayList with Route objects
	 * 
	 * @param response
	 */
	private void populateListOfRoutes(final JSONObject response){
		this.listOfRoutesToShow = new ArrayList<Route>();
		try {
			JSONArray jsonArray = response.getJSONArray("rows");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				Route route = new Route(object.getString("id"), object.getString("longName"), object.getString("shortName"));
				this.listOfRoutesToShow.add(route);
			}
		} catch (JSONException e) {
			// If jsonArray hasn't rows, then there is error on server communication (considered internet connection error) 
			Log.e("Error to populate list of routes", e.toString());
			ProgressDialog.show(ListSearch.this, "Error", getString(R.string.internet_connection_error), false, true);
		}
		this.showListOfRoutesOrNotFoundToUser();
	}
	
	/**
	 * If there are routes, show that, else show not found message to user
	 * 
	 * @param routesToShow
	 */
	private void showListOfRoutesOrNotFoundToUser(){
		if(listOfRoutesToShow.size() > 0){
			this.showRoutesList();
			RoutesArrayAdapter adapter = new RoutesArrayAdapter(this, layout.layout_list_routes, this.listOfRoutesToShow);
			this.listOfRoutesView.setAdapter(adapter);
			this.listOfRoutesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Route selectedRoute = (Route) parent.getItemAtPosition(position);
					Log.i("SelectedRoute", selectedRoute.getId());
					ListSearch.this.showRouteDetails(selectedRoute);
				}
			});
		} else {
			this.showNoRouteFoundMessage();
		}
	}
	
	/**
	 * Show no found route message and hide empty routesList and loading message
	 */
	private void showNoRouteFoundMessage(){
		this.loading.setVisibility(View.GONE);
		this.listOfRoutesView.setVisibility(View.GONE);
		this.noRouteFound.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Show routesList and hide no found route and loading message
	 */
	private void showRoutesList(){
		this.loading.setVisibility(View.GONE);
		this.noRouteFound.setVisibility(View.GONE);
		this.listOfRoutesView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Recover selectedRouteId and start new activity RouteDetail, sending the values of 
	 * selectedRouteId, selectedRouteShortName and selectedRouteLongName.
	 * 
	 * @param numberAndNameRoute
	 */
	private void showRouteDetails(final Route selectedRoute){
		// sending selectedRouteId to activity RouteDetail
		Intent intent = new Intent(ListSearch.this, RouteDetail.class);
		Bundle bundle = new Bundle();
		bundle.putString("selectedRouteId", selectedRoute.getId());
		bundle.putString("selectedRouteShortName", selectedRoute.getShortName());
		bundle.putString("selectedRouteLongName", selectedRoute.getLongName());
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	/**
	 * Execute in background. Access server to order list of routes. 
	 * This asynchronous task allow maintain a responsive user interface and publish progress.
	 * 
	 * @author Henio
	 * @since 2014/02
	 */
	private class FetchServerRoutes extends AsyncTask<JSONObject, String, JSONObject>{
		
		/**
		 * Executed before server communication. Show loading message to user.
		 */
		@Override
		protected void onPreExecute() {
			ListSearch.this.noRouteFound.setVisibility(View.GONE);
			ListSearch.this.listOfRoutesView.setVisibility(View.GONE);
			ListSearch.this.loading.setVisibility(View.VISIBLE);
		}
		
		/**
		 * Access server to order list of routes.
		 */
		@Override
		protected JSONObject doInBackground(JSONObject... params) {
			JSONObject result = ServerCommunication.postData(ListSearch.URL_FIND_ROUTES, params[0]);
			return result;
		}
		
		/**
		 * Executed after server communication.
		 * Return to ListSearch to populate results on interface.
		 */
		@Override
		protected void onPostExecute(JSONObject result) {
			ListSearch.this.populateListOfRoutes(result);
		}
	}

}

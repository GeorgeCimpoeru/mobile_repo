package com.poc.p_couds.fragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.poc.p_couds.APIClient;
import com.poc.p_couds.R;
import com.poc.p_couds.models.IApiService;
import com.poc.p_couds.pojo.Authenticate;
import com.poc.p_couds.pojo.ChangeStatus;
import com.poc.p_couds.pojo.ChangeStatusSession;
import com.poc.p_couds.pojo.ECU;
import com.poc.p_couds.pojo.FileNode;
import com.poc.p_couds.pojo.GetIdentifiers;
import com.poc.p_couds.pojo.ReadAccesTiming;
import com.poc.p_couds.pojo.ReadAccessTimingPost;
import com.poc.p_couds.pojo.ResetEcu;
import com.poc.p_couds.pojo.ResetEcuPost;
import com.poc.p_couds.pojo.TesterPresent;
import com.poc.p_couds.pojo.WriteBatteryChargeStateDataClass;
import com.poc.p_couds.pojo.WriteBatteryLevelDataClass;
import com.poc.p_couds.pojo.WriteBatteryPercentageDataClass;
import com.poc.p_couds.pojo.WriteBatteryVoltageDataClass;
import com.poc.p_couds.pojo.WriteDoorsAjarDataClass;
import com.poc.p_couds.pojo.WriteDoorsDoorDataClass;
import com.poc.p_couds.pojo.WriteDoorsPassengerDataClass;
import com.poc.p_couds.pojo.WriteDoorsPassengerLockDataClass;
import com.poc.p_couds.pojo.WriteEngineCoolantTempDataClass;
import com.poc.p_couds.pojo.WriteEngineFuelLevelDataClass;
import com.poc.p_couds.pojo.WriteEngineFuelPressureDataClass;
import com.poc.p_couds.pojo.WriteEngineIntakeAirTempDataClass;
import com.poc.p_couds.pojo.WriteEngineLoadDataClass;
import com.poc.p_couds.pojo.WriteEngineOilTempDataClass;
import com.poc.p_couds.pojo.WriteEngineRpmDataClass;
import com.poc.p_couds.pojo.WriteEngineSpeedDataClass;
import com.poc.p_couds.pojo.WriteEngineThrottlePosDataClass;
import com.poc.p_couds.pojo.WriteHvacAmbientAirTempDataClass;
import com.poc.p_couds.pojo.WriteHvacCabinTempDataClass;
import com.poc.p_couds.pojo.WriteHvacCabinTempDriverSetDataClass;
import com.poc.p_couds.pojo.WriteHvacFanSpeedDataClass;
import com.poc.p_couds.pojo.WriteHvacMassAirFlowDataClass;
import com.poc.p_couds.pojo.WriteHvacModesDataClass;
import com.poc.p_couds.pojo.WriteTiming;
import com.poc.p_couds.pojo.WriteTimingPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import static kotlinx.coroutines.BuildersKt.runBlocking;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_RequestSendAutomated#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_RequestSendAutomated extends Fragment {
    private TextView testerTextView;
    private IApiService apiInterface;

    List<String> allEcus = Arrays.asList(new String[]{"MCU", "Battery", "Engine", "Doors", "HVAC"});
    List<String> displayedEcus = new ArrayList<>(Arrays.asList(new String[]{"None"}));
    List<ECU.ECUDetail> listOfEcus = new ArrayList<>();
    String mcuId = "";
    ArrayAdapter<String>  adapterEcuReset;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_send_automated, container, false);
        testerTextView = view.findViewById(R.id.response);
        testerTextView.setMovementMethod(new ScrollingMovementMethod());

        SwitchCompat switchCompatSession = view.findViewById(R.id.change_session);
        switchCompatSession.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int subFunct = isChecked ? 2 : 1;
            ChangeStatusSession postBodySession = new ChangeStatusSession(subFunct);
            toggleSession(postBodySession);
            if (subFunct == 2) {
                Toast.makeText(Fragment_RequestSendAutomated.this.getContext(), "PROGRAMMING SESSION", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Fragment_RequestSendAutomated.this.getContext(), "DEFAULT SESSION", Toast.LENGTH_SHORT).show();
            }

        });

        Button docsButton = view.findViewById(R.id.docs);
        docsButton.setOnClickListener(v -> redirectToDocs());

        Button checkVersions = view.findViewById(R.id.q2);
        checkVersions.setOnClickListener(v -> callCheckVersions());

        Button authentication = view.findViewById(R.id.q3);
        authentication.setOnClickListener(v -> callApiAuthentication());

        Button readIdentifiers = view.findViewById(R.id.q4);
        readIdentifiers.setOnClickListener(v -> callApiIdentifiers());

        Button requestId = view.findViewById(R.id.q5);
        requestId.setOnClickListener(v -> callRequestId());

        getEcuAndIds();

        Spinner ecuReset = view.findViewById(R.id.ecu_reset_spinner);
        adapterEcuReset = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, displayedEcus);
        ecuReset.setAdapter(adapterEcuReset);

        List<String> typeResetList = Arrays.asList(new String[]{"Type","soft", "hard"});
        Spinner typeReset = view.findViewById(R.id.type_reset_spinner);
        ArrayAdapter<String>  adapterTypeReset = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, typeResetList);
        typeReset.setAdapter(adapterTypeReset);
        typeReset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(view.getContext(), "Reset", Toast.LENGTH_SHORT).show();
                String ecuId;
                if (i == 0)
                {
                    return;
                }
                if (ecuReset.getSelectedItemPosition() > 0)
                {
                    ecuId = listOfEcus.get(ecuReset.getSelectedItemPosition()).getEcu_id();
                }
                else
                {
                    ecuId = mcuId;
                }
                String typeOfReset = typeResetList.get(i);
                ResetEcuPost r = new ResetEcuPost(ecuId, typeOfReset);
                callEcuReset(r);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        SwitchCompat switchCompatTester = view.findViewById(R.id.tester);
        switchCompatTester.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                toggleTester();
                Log.d("Toggle", "ON");
            } else {
                Log.d("Toggle", "OFF");
            }
        });
        SwitchCompat switchCompatReadAccess = view.findViewById(R.id.read_access);
        switchCompatReadAccess.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int subFunct = isChecked ? 3 : 1; // If checked, subFunct = 3; else subFunct = 1

            // Create the POST body object
            ReadAccessTimingPost postBody = new ReadAccessTimingPost(subFunct);
            toggleRead(postBody);
        });

        Button writeTimingP2Max = view.findViewById(R.id.q6);
        writeTimingP2Max.setOnClickListener(v -> showInputDialogTiming());

        Button dropdownReadBatteryBtn = view.findViewById(R.id.dropdown_button_read_battery);
        dropdownReadBatteryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), dropdownReadBatteryBtn);
                popup.getMenuInflater().inflate(R.menu.read_battery_manu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.read_battery_all) {
                            readEcusInfo("battery", "");
                            Toast.makeText(getContext(), "Read battery all", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_battery_level) {
                            readEcusInfo("battery", "battery_level");
                            Toast.makeText(getContext(), "Read battery level", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_battery_state_of_charge) {
                            readEcusInfo("battery", "battery_state_of_charge");
                            Toast.makeText(getContext(), "Read battery state of charge", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_battery_percentage) {
                            readEcusInfo("battery", "percentage");
                            Toast.makeText(getContext(), "Read battery percentage", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_battery_voltage) {
                            readEcusInfo("battery", "voltage");
                            Toast.makeText(getContext(), "Read battery voltage", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        Button dropdownWriteBatteryBtn = view.findViewById(R.id.dropdown_button_write_battery);
        dropdownWriteBatteryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), dropdownWriteBatteryBtn);
                popup.getMenuInflater().inflate(R.menu.write_battery_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        String ecu = "battery";
                        if (itemId == R.id.write_battery_level) {
                            showCustomDialog(ecu, "Level");
                            Toast.makeText(getContext(), "Write battery level", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_battery_state_of_charge) {
                            showCustomDialog(ecu, "State of charge");
                            Toast.makeText(getContext(), "Write battery state of charge", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_battery_percentage) {
                            showCustomDialog(ecu, "Percentage");
                            Toast.makeText(getContext(), "Write battery percentage", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_battery_voltage) {
                            showCustomDialog(ecu, "Voltage");
                            Toast.makeText(getContext(), "Write battery voltage", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        Button dropdownReadEngineBtn = view.findViewById(R.id.dropdown_button_read_engine);
        dropdownReadEngineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), dropdownReadEngineBtn);
                popup.getMenuInflater().inflate(R.menu.read_engine_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.read_engine_all) {
                            readEcusInfo("engine", "");
                            Toast.makeText(getContext(), "Read engine all", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_coolant_temperature) {
                            readEcusInfo("engine", "coolant_temperature");
                            Toast.makeText(getContext(), "Read engine coolant temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_load) {
                            readEcusInfo("engine", "engine_load");
                            Toast.makeText(getContext(), "Read engine load", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_RPM) {
                            readEcusInfo("engine", "engine_rpm");
                            Toast.makeText(getContext(), "Read engine RPM", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_fuel_level) {
                            readEcusInfo("engine", "fuel_level");
                            Toast.makeText(getContext(), "Read engine fuel level", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_fuel_pressure) {
                            readEcusInfo("engine", "fuel_pressure");
                            Toast.makeText(getContext(), "Read engine fuel pressure", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_intake_air_temperature) {
                            readEcusInfo("engine", "intake_air_temperature");
                            Toast.makeText(getContext(), "Read engine intake air temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_oil_temperature) {
                            readEcusInfo("engine", "oil_temperature");
                            Toast.makeText(getContext(), "Read engine oil temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_throttle_position) {
                            readEcusInfo("engine", "throttle_position");
                            Toast.makeText(getContext(), "Read engine throttle position", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_engine_vehicle_speed) {
                            readEcusInfo("engine", "vehicle_speed");
                            Toast.makeText(getContext(), "Read engine vehicle speed", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        Button dropdownWriteEngineBtn = view.findViewById(R.id.dropdown_button_write_engine);
        dropdownWriteEngineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), dropdownWriteEngineBtn);
                popup.getMenuInflater().inflate(R.menu.write_engine_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        String ecu = "engine";
                        if (itemId == R.id.write_engine_coolant_temperature) {
                            showCustomDialog(ecu, "Coolant temperature");
                            Toast.makeText(getContext(), "Write engine coolant temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_engine_load) {
                            showCustomDialog(ecu, "Engine load");
                            Toast.makeText(getContext(), "Write engine load", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_engine_RPM) {
                            showCustomDialog(ecu, "Engine RPM");
                            Toast.makeText(getContext(), "Write engine RPM", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_engine_fuel_level) {
                            showCustomDialog(ecu, "Fuel level");
                            Toast.makeText(getContext(), "Write engine fuel level", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_engine_fuel_pressure) {
                            showCustomDialog(ecu, "Fuel pressure");
                            Toast.makeText(getContext(), "Write engine fuel pressure", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_engine_intake_air_temperature) {
                            showCustomDialog(ecu, "Intake air temperature");
                            Toast.makeText(getContext(), "Write engine intake air temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_engine_oil_temperature) {
                            showCustomDialog(ecu, "Oil temperature");
                            Toast.makeText(getContext(), "Write engine oil temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_engine_throttle_position) {
                            showCustomDialog(ecu, "Throttle position");
                            Toast.makeText(getContext(), "Write engine throttle position", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_engine_vehicle_speed) {
                            showCustomDialog(ecu, "Vehicle speed");
                            Toast.makeText(getContext(), "Write engine vehicle speed", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        Button dropdownReadDoorsBtn = view.findViewById(R.id.dropdown_button_read_doors);
        dropdownReadDoorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), dropdownReadDoorsBtn);
                popup.getMenuInflater().inflate(R.menu.read_doors_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.read_doors_all) {
                            readEcusInfo("doors", "");
                            Toast.makeText(getContext(), "Read doors all", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_doors_ajar) {
                            readEcusInfo("doors", "ajar");
                            Toast.makeText(getContext(), "Read doors ajar", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_doors_state_of_door) {
                            readEcusInfo("doors", "door");
                            Toast.makeText(getContext(), "Read doors state", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_doors_passenger) {
                            readEcusInfo("doors", "passenger");
                            Toast.makeText(getContext(), "Read doors passenger", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_doors_passenger_lock) {
                            readEcusInfo("doors", "passenger_lock");
                            Toast.makeText(getContext(), "Read doors passenger lock", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        Button dropdownWriteDoorsBtn = view.findViewById(R.id.dropdown_button_write_doors);
        dropdownWriteDoorsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), dropdownWriteDoorsBtn);
                popup.getMenuInflater().inflate(R.menu.write_doors_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        String ecu = "doors";
                        if (itemId == R.id.write_doors_ajar) {
                            showCustomDialog(ecu, "Ajar");
                            Toast.makeText(getContext(), "Write doors ajar", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_doors_state_of_door) {
                            showCustomDialog(ecu, "Doors state");
                            Toast.makeText(getContext(), "Write doors state", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_doors_passenger) {
                            showCustomDialog(ecu, "Passenger");
                            Toast.makeText(getContext(), "Write doors passenger", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_doors_passenger_lock) {
                            showCustomDialog(ecu, "Passenger lock");
                            Toast.makeText(getContext(), "Write doors passenger lock", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        Button dropdownReadHvacBtn = view.findViewById(R.id.dropdown_button_read_hvac);
        dropdownReadHvacBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), dropdownReadHvacBtn);
                popup.getMenuInflater().inflate(R.menu.read_hvac_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.read_hvac_all) {
                            readEcusInfo("hvac", "");
                            Toast.makeText(getContext(), "Read hvac all", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_hvac_ambient_air_temperature) {
                            readEcusInfo("hvac", "ambient_air_temperature");
                            Toast.makeText(getContext(), "Read hvac ambient air temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_hvac_cabin_temperature) {
                            readEcusInfo("hvac", "cabin_temperature");
                            Toast.makeText(getContext(), "Read hvac cabin temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_hvac_driver_seat_temperature) {
                            readEcusInfo("hvac", "cabin_temperature_driver_set");
                            Toast.makeText(getContext(), "Read hvac driver seat temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_hvac_fan_speed) {
                            readEcusInfo("hvac", "fan_speed");
                            Toast.makeText(getContext(), "Read hvac fan speed", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.read_hvac_mass_air_flow) {
                            readEcusInfo("hvac", "mass_air_flow");
                            Toast.makeText(getContext(), "Read hvac mass air flow", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        Button dropdownWriteHvacBtn = view.findViewById(R.id.dropdown_button_write_hvac);
        dropdownWriteHvacBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), dropdownWriteHvacBtn);
                popup.getMenuInflater().inflate(R.menu.write_hvac_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        String ecu = "hvac";
                        if (itemId == R.id.write_hvac_ambient_air_temperature) {
                            showCustomDialog(ecu, "Ambient air temperature");
                            Toast.makeText(getContext(), "Write hvac ambient air temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_hvac_cabin_temperature) {
                            showCustomDialog(ecu, "Cabin temperature");
                            Toast.makeText(getContext(), "Write hvac cabin temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_hvac_driver_seat_temperature) {
                            showCustomDialog(ecu, "Driver seat temperature");
                            Toast.makeText(getContext(), "Write hvac driver seat temperature", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_hvac_fan_speed) {
                            showCustomDialog(ecu, "Fan speed");
                            Toast.makeText(getContext(), "Write hvac fan speed", Toast.LENGTH_SHORT).show();
                        } else if (itemId == R.id.write_hvac_mass_air_flow) {
                            showCustomDialog(ecu, "Mass air flow");
                            Toast.makeText(getContext(), "Write hvac mass air flow", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });

        return view;
    }

    private void writeEcusInfo(String ecu, String param, String newValue) {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call call = null;
        Object writeEcusInfoRequest = new Object();
        if (Objects.equals(ecu, "battery")) {
            switch (param) {
                case "Level":
                    writeEcusInfoRequest = new WriteBatteryLevelDataClass(newValue, false);
                    break;
                case "State of charge":
                    writeEcusInfoRequest = new WriteBatteryChargeStateDataClass(newValue, false);
                    break;
                case "Percentage":
                    writeEcusInfoRequest = new WriteBatteryPercentageDataClass(newValue, false);
                    break;
                case "Voltage":
                    writeEcusInfoRequest = new WriteBatteryVoltageDataClass(newValue, false);
                    break;
                default:
                    break;
            }
            call = apiInterface.writeInfoBatteryForJavaCompatibility(writeEcusInfoRequest);
        } else if (Objects.equals(ecu, "engine")) {
            switch (param) {
                case "Coolant temperature":
                    writeEcusInfoRequest = new WriteEngineCoolantTempDataClass(newValue, false);
                    break;
                case "Engine load":
                    writeEcusInfoRequest = new WriteEngineLoadDataClass(newValue, false);
                    break;
                case "Engine RPM":
                    writeEcusInfoRequest = new WriteEngineRpmDataClass(newValue, false);
                    break;
                case "Fuel level":
                    writeEcusInfoRequest = new WriteEngineFuelLevelDataClass(newValue, false);
                    break;
                case "Fuel pressure":
                    writeEcusInfoRequest = new WriteEngineFuelPressureDataClass(newValue, false);
                    break;
                case "Intake air temperature":
                    writeEcusInfoRequest = new WriteEngineIntakeAirTempDataClass(newValue, false);
                    break;
                case "Oil temperature":
                    writeEcusInfoRequest = new WriteEngineOilTempDataClass(newValue, false);
                    break;
                case "Throttle position":
                    writeEcusInfoRequest = new WriteEngineThrottlePosDataClass(newValue, false);
                    break;
                case "Vehicle speed":
                    writeEcusInfoRequest = new WriteEngineSpeedDataClass(newValue, false);
                    break;
                default:
                    break;
            }
            call = apiInterface.writeInfoEngineForJavaCompatibility(writeEcusInfoRequest);
        } else if (Objects.equals(ecu, "doors")) {
            switch (param) {
                case "Ajar":
                    writeEcusInfoRequest = new WriteDoorsAjarDataClass(newValue, false);
                    break;
                case "Doors state":
                    writeEcusInfoRequest = new WriteDoorsDoorDataClass(newValue, false);
                    break;
                case "Passenger":
                    writeEcusInfoRequest = new WriteDoorsPassengerDataClass(newValue, false);
                    break;
                case "Passenger lock":
                    writeEcusInfoRequest = new WriteDoorsPassengerLockDataClass(newValue, false);
                    break;
                default:
                    break;
            }
            call = apiInterface.writeInfoDoorsForJavaCompatibility(writeEcusInfoRequest);
        } else if (Objects.equals(ecu, "hvac")) {
            switch (param) {
                case "Ambient air temperature":
                    writeEcusInfoRequest = new WriteHvacAmbientAirTempDataClass(newValue, false);
                    break;
                case "Cabin temperature":
                    writeEcusInfoRequest = new WriteHvacCabinTempDataClass(newValue, false);
                    break;
                case "Driver seat temperature":
                    writeEcusInfoRequest = new WriteHvacCabinTempDriverSetDataClass(newValue, false);
                    break;
                case "Fan speed":
                    writeEcusInfoRequest = new WriteHvacFanSpeedDataClass(newValue, false);
                    break;
                case "AC status":
                case "Air recirc":
                case "Defrost":
                case "Front":
                case "Legs":
                    writeEcusInfoRequest = new WriteHvacModesDataClass(newValue, false);
                    break;
                case "Mass air flow":
                    writeEcusInfoRequest = new WriteHvacMassAirFlowDataClass(newValue, false);
                    break;
                default:
                    break;
            }
            call = apiInterface.writeInfoHVACForJavaCompatibility(writeEcusInfoRequest);
        }
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        testerTextView.setText(jsonObject.toString(4));
                    } catch (JSONException e) {
                        testerTextView.setText(jsonResponse);
                        throw new RuntimeException(e);
                    }
                }
                else {
                    testerTextView.setText("Failed to retrive response");
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                testerTextView.setText("Request Failed: " + t);
            }
        });
    }

    private void showCustomDialog(String ecu, String param) {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.send_requests_write_ecus_dialog, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Get dialog components
        TextView textViewTitle = dialogView.findViewById(R.id.textViewTitle);
        textViewTitle.setText(param);

        EditText editTextInput = dialogView.findViewById(R.id.editTextInput);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newValue = editTextInput.getText().toString();
                writeEcusInfo(ecu, param, newValue);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void readEcusInfo(String ecu, String item) {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call<?> call = apiInterface.getInfoBattery(false, item);
        if (Objects.equals(ecu, "engine")) {
            call = apiInterface.getInfoEngine(false, item);
        } else if (Objects.equals(ecu, "doors")) {
            call = apiInterface.getInfoDoors(false, item);
        } else if (Objects.equals(ecu, "hvac")) {
            call = apiInterface.getInfoHVAC(false, item);
        }

        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        testerTextView.setText(jsonObject.toString(4));
                    } catch (JSONException e) {
                        testerTextView.setText(jsonResponse);
                        throw new RuntimeException(e);
                    }
                }
                else {
                    testerTextView.setText("Failed to retrive response");
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                testerTextView.setText("Request Failed: " + t);
            }
        });
    }

    private void toggleSession(ChangeStatusSession postBodySession) {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call<ChangeStatus> call=apiInterface.requestChangeStatus(postBodySession);
        call.enqueue(new Callback<ChangeStatus>() {
            @Override
            public void onResponse(Call<ChangeStatus> call, Response<ChangeStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    testerTextView.setText(jsonResponse);
                }
                else {
                    testerTextView.setText("Failed to retrive response");
                }
            }
            @Override
            public void onFailure(Call<ChangeStatus> call, Throwable t) {
                testerTextView.setText("Request Failed: " + t);
            }
        });
    }

    private void redirectToDocs() {
        Intent browserIntent = new Intent(Intent. ACTION_VIEW, Uri.parse("https://github.com/GeorgeCimpoeru/PoC/blob/master/README.md"));
        startActivity(browserIntent);
    }

    private void getEcuAndIds() {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call<ECU> call=apiInterface.requestListOfEcus();
        call.enqueue(new Callback<ECU>() {
            @Override
            public void onResponse(Call<ECU> call, Response<ECU> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ECU ecusResponse = response.body();
                    listOfEcus = ecusResponse.getEcus();
                    mcuId = ecusResponse.getMcuId();
                    String status = ecusResponse.getStatus();
                    if (Objects.equals(status, "Success")) {
                        displayedEcus.addAll(allEcus.subList(0, listOfEcus.size() + 1));
                        adapterEcuReset.notifyDataSetChanged();
                    } else{
                        Log.w("PoC", "Unable to retrieve ECUs available from api...");
                        Toast.makeText(getContext(), "Unable to retrieve ECUs available from api...", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Log.e("PoC","Error while making call to retrieve ecu ids information. Code:" + response.code());
                }
            }

            @Override
            public void onFailure(Call<ECU> call, Throwable t) {
                Log.e("PoC","Error while making call to retrieve ecu ids information...");
            }
        });
    }

    private void callEcuReset(ResetEcuPost ecu) {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call<ResetEcu> call = apiInterface.requestResetEcu(ecu);
        call.enqueue(new Callback<ResetEcu>() {
            @Override
            public void onResponse(Call<ResetEcu> call, Response<ResetEcu> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    testerTextView.setText(jsonResponse);
                }
                else {
                    testerTextView.setText("Failed to retrive response");
                }
            }

            @Override
            public void onFailure(Call<ResetEcu> call, Throwable t) {
                testerTextView.setText("Request Failed: " + t.getMessage());
            }
        });
    }

    private void callCheckVersions() {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call<FileNode> call=apiInterface.requestListOfVersions();
        call.enqueue(new Callback<FileNode>() {
            @Override
            public void onResponse(Call<FileNode> call, Response<FileNode> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    testerTextView.setText(jsonResponse);
                }
                else {
                    testerTextView.setText("Failed to retrive response");
                }
            }

            @Override
            public void onFailure(Call<FileNode> call, Throwable t) {
                testerTextView.setText("Request Failed: " + t.getMessage());
            }
        });
    }

    private void callRequestId() {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call<ECU> call=apiInterface.requestListOfEcus();
        call.enqueue(new Callback<ECU>() {
            @Override
            public void onResponse(Call<ECU> call, Response<ECU> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    testerTextView.setText(jsonResponse);
                }
                else {
                    testerTextView.setText("Failed to retrive response");
                }
            }

            @Override
            public void onFailure(Call<ECU> call, Throwable t) {
                testerTextView.setText("Request Failed: " + t.getMessage());
            }
        });
    }

    private void callApiAuthentication() {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call<Authenticate> call=apiInterface.requestAuthenticate();
        call.enqueue(new Callback<Authenticate>() {
            @Override
            public void onResponse(Call<Authenticate> call, Response<Authenticate> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    testerTextView.setText(jsonResponse);
                }
                else {
                    testerTextView.setText("Failed to retrive response");
                }
            }
            @Override
            public void onFailure(Call<Authenticate> call, Throwable t) {
                testerTextView.setText("Request Failed: " + t.getMessage());
            }
        });
    }

    private void callApiIdentifiers() {
        apiInterface = APIClient.getClient().create(IApiService.class);

        Call<GetIdentifiers> call = apiInterface.requestGetIdentifiers();
        call.enqueue(new Callback<GetIdentifiers>() {
            @Override
            public void onResponse(Call<GetIdentifiers> call, Response<GetIdentifiers> response) {
                if (response.isSuccessful() && response != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    testerTextView.setText(jsonResponse);
                } else {
                    testerTextView.setText("Failed to retrieve response");
                }
            }
            @Override
            public void onFailure(Call<GetIdentifiers> call, Throwable t) {
                testerTextView.setText("Request Failed: "+ t.getMessage());
            }
        });
    }

    private void toggleTester() {
        apiInterface = APIClient.getClient().create(IApiService.class);
        Call<TesterPresent> call = apiInterface.requestTesterPresent();
        call.enqueue(new Callback<TesterPresent>() {
            @Override
            public void onResponse(Call<TesterPresent> call, Response<TesterPresent> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    testerTextView.setText(jsonResponse);
                } else {
                    testerTextView.setText("Failed to retrieve response.");
                }
            }

            @Override
            public void onFailure(Call<TesterPresent> call, Throwable t) {
                testerTextView.setText("Request failed: " + t.getMessage());
            }
        });
    }

    private void toggleRead(ReadAccessTimingPost postBody) {
        apiInterface = APIClient.getClient().create(IApiService.class);
        Call<ReadAccesTiming> call = apiInterface.requestReadAccessTiming(postBody);
        call.enqueue(new Callback<ReadAccesTiming>() {
            @Override
            public void onResponse(Call<ReadAccesTiming> call, Response<ReadAccesTiming> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());
                    testerTextView.setText(jsonResponse);
                } else {
                    testerTextView.setText("Failed to retrieve response.");
                }
            }

            @Override
            public void onFailure(Call<ReadAccesTiming> call, Throwable t) {
                testerTextView.setText("Request failed: " + t.getMessage());
            }
        });
    }

    private void showInputDialogTiming() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.dialog_input_p2time, null);

        EditText editTextP2MaxTime = dialogView.findViewById(R.id.editTextP2MaxTime);
        EditText editTextP2StarTime = dialogView.findViewById(R.id.editTextP2StarMaxTime);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("P2 Timing Values");
        builder.setView(dialogView);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String p2MaxTimeStr = editTextP2MaxTime.getText().toString();
                String p2StarTimeStr = editTextP2StarTime.getText().toString();
                ;

                if (!p2MaxTimeStr.isEmpty() && !p2StarTimeStr.isEmpty()) {
                    try {
                        // Convert to integers
                        int p2MaxTime = Integer.parseInt(p2MaxTimeStr);
                        int p2StarTime = Integer.parseInt(p2StarTimeStr);

                        // Call the API with integer values
                        callApiWithCustomTiming(p2MaxTime, p2StarTime);
                    } catch (NumberFormatException e) {
                        // Show an error message for invalid integer input
                        testerTextView.setText("Please enter valid integer values.");
                    }
                } else {
                    testerTextView.setText("Please enter values for both fields.");
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void callApiWithCustomTiming(int p2MaxTime, int p2StarTime) {
        apiInterface = APIClient.getClient().create(IApiService.class);

        WriteTimingPost postBody = new WriteTimingPost(p2MaxTime, p2StarTime);

        Call<WriteTiming> call= apiInterface.requestWriteTiming(postBody);
        call.enqueue(new Callback<WriteTiming>() {
            @Override
            public void onResponse(Call<WriteTiming> call, Response<WriteTiming> response){
                if (response.isSuccessful() && response.body() != null) {
                    Gson gson = new Gson();
                    String jsonResponse = gson.toJson(response.body());

                    testerTextView.setText(jsonResponse);
                }
                else {
                    testerTextView.setText("Failed to retrieve the response.");
                }
            }

            @Override
            public void onFailure(Call<WriteTiming> call, Throwable t) {
                testerTextView.setText("Request failed: " + t.getMessage());
            }
        });
    }
}

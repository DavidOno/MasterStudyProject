package de.smarthome.server.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.smarthome.R;
import de.smarthome.model.impl.Function;
import de.smarthome.model.viewmodel.RoomOverviewViewModel;
import de.smarthome.server.adapter.RoomOverviewAdapter;
import de.smarthome.server.adapter.TestAdapter;

public class RoomOverviewFragment extends Fragment {
    private  final String TAG = "RoomOverviewFragment";
    private RoomOverviewViewModel roomOverviewViewModel;
    private RecyclerView recyclerViewRoom;

    private String roomName;
    private String roomUID;
    //private RoomOverviewAdapter adapter;
    private TestAdapter adapter;

    public static RoomOverviewFragment newInstance() {
        RoomOverviewFragment fragment = new RoomOverviewFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_overview, container, false);

        recyclerViewRoom = view.findViewById(R.id.recycler_view_room_overview);
        roomOverviewViewModel = new ViewModelProvider(requireActivity()).get(RoomOverviewViewModel.class);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomUID = RoomOverviewFragmentArgs.fromBundle(getArguments()).getIDSelectedRoom();
        roomName = RoomOverviewFragmentArgs.fromBundle(getArguments()).getNameSelectedRoom();


        recyclerViewRoom  = view.findViewById(R.id.recycler_view_room_overview);
        recyclerViewRoom.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRoom.setHasFixedSize(true);

        //adapter = new RoomOverviewAdapter();
        adapter = new TestAdapter();
        recyclerViewRoom.setAdapter(adapter);

        roomOverviewViewModel.getUsableRoomFunctions().observe(getViewLifecycleOwner(), new Observer<List<Function>>() {
            @Override
            public void onChanged(@Nullable List<Function> functions) {
                adapter.setFunctionList(functions);
            }
        });

        /*adapter.setOnItemClickListener(new RoomOverviewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Function function) {
                navigateToRegulationFragment(roomName, function.getID());
            }
        });*/
        adapter.setOnItemClickListener(new TestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Function function) {
               if(!roomOverviewViewModel.isChannelInputOnlyBinary(function)){
                    navigateToRegulationFragment(roomName, function.getID());
                }
            }
        });

        adapter.setOnSwitchClickListener(new TestAdapter.OnSwitchClickListener() {
            @Override
            public void onItemClick(Function function, boolean isChecked) {
                if(isChecked){
                    roomOverviewViewModel.requestSetValue(function.getID(), "1");
                }else{
                    roomOverviewViewModel.requestSetValue(function.getID(), "0");
                }
            }
        });
    }

    public void navigateToRegulationFragment(String roomName, String functionUID) {
        NavController navController = NavHostFragment.findNavController(this);

        RoomOverviewFragmentDirections.ActionRoomOverviewFragmentToRegulationFragment toRegulationFragment =
                RoomOverviewFragmentDirections.actionRoomOverviewFragmentToRegulationFragment();

        toRegulationFragment.setNameSelectedRoom(roomName);
        toRegulationFragment.setSelectedFunctionUID(functionUID);

        navController.navigate(toRegulationFragment);
    }


    public List<String> getFunctionNames(List<Function> functionList){
        List<String> outputList = new ArrayList<>();
        for(Function function: functionList){
            outputList.add(function.getName());
        }
        return outputList;
    }
}
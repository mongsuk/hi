package com.example.emulator;
import com.example.emulator.EmulatorAIDLCallback;

interface EmulatorAIDL {
	void registerCallback(EmulatorAIDLCallback cb);
	void unregisterCallback(EmulatorAIDLCallback cb); 
	void openfile();
	void closefile();
	
	}
package com.doubleclue.dcem.setup.logic;

public enum DbState {
	OK, Migration_Required, Create_Schema_Required, Create_Tables_Required, No_Connection, Exception, Init 
}

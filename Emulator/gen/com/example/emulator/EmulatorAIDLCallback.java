/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/hjeong/WindRiver_Project01.git/TestProject/Emulator/src/com/example/emulator/EmulatorAIDLCallback.aidl
 */
package com.example.emulator;
public interface EmulatorAIDLCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.example.emulator.EmulatorAIDLCallback
{
private static final java.lang.String DESCRIPTOR = "com.example.emulator.EmulatorAIDLCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.example.emulator.EmulatorAIDLCallback interface,
 * generating a proxy if needed.
 */
public static com.example.emulator.EmulatorAIDLCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.example.emulator.EmulatorAIDLCallback))) {
return ((com.example.emulator.EmulatorAIDLCallback)iin);
}
return new com.example.emulator.EmulatorAIDLCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_Start:
{
data.enforceInterface(DESCRIPTOR);
this.Start();
reply.writeNoException();
return true;
}
case TRANSACTION_Stop:
{
data.enforceInterface(DESCRIPTOR);
this.Stop();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.example.emulator.EmulatorAIDLCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void Start() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_Start, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void Stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_Stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_Start = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_Stop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public void Start() throws android.os.RemoteException;
public void Stop() throws android.os.RemoteException;
}

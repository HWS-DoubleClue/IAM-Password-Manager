/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.IO;
using Thrift;
using Thrift.Collections;
using System.Runtime.Serialization;
using Thrift.Protocol;
using Thrift.Transport;


#if !SILVERLIGHT
[Serializable]
#endif
public partial class SignatureResponse : TBase
{
  private byte[] _reconnectTicket;
  private int _keepAliveSeconds;

  public byte[] ReconnectTicket
  {
    get
    {
      return _reconnectTicket;
    }
    set
    {
      __isset.reconnectTicket = true;
      this._reconnectTicket = value;
    }
  }

  public int KeepAliveSeconds
  {
    get
    {
      return _keepAliveSeconds;
    }
    set
    {
      __isset.keepAliveSeconds = true;
      this._keepAliveSeconds = value;
    }
  }


  public Isset __isset;
  #if !SILVERLIGHT
  [Serializable]
  #endif
  public struct Isset {
    public bool reconnectTicket;
    public bool keepAliveSeconds;
  }

  public SignatureResponse() {
  }

  public void Read (TProtocol iprot)
  {
    iprot.IncrementRecursionDepth();
    try
    {
      TField field;
      iprot.ReadStructBegin();
      while (true)
      {
        field = iprot.ReadFieldBegin();
        if (field.Type == TType.Stop) { 
          break;
        }
        switch (field.ID)
        {
          case 1:
            if (field.Type == TType.String) {
              ReconnectTicket = iprot.ReadBinary();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 2:
            if (field.Type == TType.I32) {
              KeepAliveSeconds = iprot.ReadI32();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          default: 
            TProtocolUtil.Skip(iprot, field.Type);
            break;
        }
        iprot.ReadFieldEnd();
      }
      iprot.ReadStructEnd();
    }
    finally
    {
      iprot.DecrementRecursionDepth();
    }
  }

  public void Write(TProtocol oprot) {
    oprot.IncrementRecursionDepth();
    try
    {
      TStruct struc = new TStruct("SignatureResponse");
      oprot.WriteStructBegin(struc);
      TField field = new TField();
      if (ReconnectTicket != null && __isset.reconnectTicket) {
        field.Name = "reconnectTicket";
        field.Type = TType.String;
        field.ID = 1;
        oprot.WriteFieldBegin(field);
        oprot.WriteBinary(ReconnectTicket);
        oprot.WriteFieldEnd();
      }
      if (__isset.keepAliveSeconds) {
        field.Name = "keepAliveSeconds";
        field.Type = TType.I32;
        field.ID = 2;
        oprot.WriteFieldBegin(field);
        oprot.WriteI32(KeepAliveSeconds);
        oprot.WriteFieldEnd();
      }
      oprot.WriteFieldStop();
      oprot.WriteStructEnd();
    }
    finally
    {
      oprot.DecrementRecursionDepth();
    }
  }

  public override string ToString() {
    StringBuilder __sb = new StringBuilder("SignatureResponse(");
    bool __first = true;
    if (ReconnectTicket != null && __isset.reconnectTicket) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("ReconnectTicket: ");
      __sb.Append(ReconnectTicket);
    }
    if (__isset.keepAliveSeconds) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("KeepAliveSeconds: ");
      __sb.Append(KeepAliveSeconds);
    }
    __sb.Append(")");
    return __sb.ToString();
  }

}


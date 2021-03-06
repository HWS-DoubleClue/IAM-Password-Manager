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
public partial class AppException : TException, TBase
{
  private string _error;
  private string _info;

  public string Error
  {
    get
    {
      return _error;
    }
    set
    {
      __isset.error = true;
      this._error = value;
    }
  }

  public string Info
  {
    get
    {
      return _info;
    }
    set
    {
      __isset.info = true;
      this._info = value;
    }
  }


  public Isset __isset;
  #if !SILVERLIGHT
  [Serializable]
  #endif
  public struct Isset {
    public bool error;
    public bool info;
  }

  public AppException() {
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
              Error = iprot.ReadString();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 2:
            if (field.Type == TType.String) {
              Info = iprot.ReadString();
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
      TStruct struc = new TStruct("AppException");
      oprot.WriteStructBegin(struc);
      TField field = new TField();
      if (Error != null && __isset.error) {
        field.Name = "error";
        field.Type = TType.String;
        field.ID = 1;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(Error);
        oprot.WriteFieldEnd();
      }
      if (Info != null && __isset.info) {
        field.Name = "info";
        field.Type = TType.String;
        field.ID = 2;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(Info);
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
    StringBuilder __sb = new StringBuilder("AppException(");
    bool __first = true;
    if (Error != null && __isset.error) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("Error: ");
      __sb.Append(Error);
    }
    if (Info != null && __isset.info) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("Info: ");
      __sb.Append(Info);
    }
    __sb.Append(")");
    return __sb.ToString();
  }

}


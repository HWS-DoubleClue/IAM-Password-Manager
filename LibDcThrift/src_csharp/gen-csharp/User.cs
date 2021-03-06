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
public partial class User : TBase
{
  private string _loginId;
  private bool _updatePushNotification;
  private bool _usingReverseProxy;

  public string LoginId
  {
    get
    {
      return _loginId;
    }
    set
    {
      __isset.loginId = true;
      this._loginId = value;
    }
  }

  public bool UpdatePushNotification
  {
    get
    {
      return _updatePushNotification;
    }
    set
    {
      __isset.updatePushNotification = true;
      this._updatePushNotification = value;
    }
  }

  public bool UsingReverseProxy
  {
    get
    {
      return _usingReverseProxy;
    }
    set
    {
      __isset.usingReverseProxy = true;
      this._usingReverseProxy = value;
    }
  }


  public Isset __isset;
  #if !SILVERLIGHT
  [Serializable]
  #endif
  public struct Isset {
    public bool loginId;
    public bool updatePushNotification;
    public bool usingReverseProxy;
  }

  public User() {
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
              LoginId = iprot.ReadString();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 2:
            if (field.Type == TType.Bool) {
              UpdatePushNotification = iprot.ReadBool();
            } else { 
              TProtocolUtil.Skip(iprot, field.Type);
            }
            break;
          case 3:
            if (field.Type == TType.Bool) {
              UsingReverseProxy = iprot.ReadBool();
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
      TStruct struc = new TStruct("User");
      oprot.WriteStructBegin(struc);
      TField field = new TField();
      if (LoginId != null && __isset.loginId) {
        field.Name = "loginId";
        field.Type = TType.String;
        field.ID = 1;
        oprot.WriteFieldBegin(field);
        oprot.WriteString(LoginId);
        oprot.WriteFieldEnd();
      }
      if (__isset.updatePushNotification) {
        field.Name = "updatePushNotification";
        field.Type = TType.Bool;
        field.ID = 2;
        oprot.WriteFieldBegin(field);
        oprot.WriteBool(UpdatePushNotification);
        oprot.WriteFieldEnd();
      }
      if (__isset.usingReverseProxy) {
        field.Name = "usingReverseProxy";
        field.Type = TType.Bool;
        field.ID = 3;
        oprot.WriteFieldBegin(field);
        oprot.WriteBool(UsingReverseProxy);
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
    StringBuilder __sb = new StringBuilder("User(");
    bool __first = true;
    if (LoginId != null && __isset.loginId) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("LoginId: ");
      __sb.Append(LoginId);
    }
    if (__isset.updatePushNotification) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("UpdatePushNotification: ");
      __sb.Append(UpdatePushNotification);
    }
    if (__isset.usingReverseProxy) {
      if(!__first) { __sb.Append(", "); }
      __first = false;
      __sb.Append("UsingReverseProxy: ");
      __sb.Append(UsingReverseProxy);
    }
    __sb.Append(")");
    return __sb.ToString();
  }

}


/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.doubleclue.comm.thrift;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2021-10-21")
public class ProxyOpenParam implements org.apache.thrift.TBase<ProxyOpenParam, ProxyOpenParam._Fields>, java.io.Serializable, Cloneable, Comparable<ProxyOpenParam> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ProxyOpenParam");

  private static final org.apache.thrift.protocol.TField HANDLE_FIELD_DESC = new org.apache.thrift.protocol.TField("handle", org.apache.thrift.protocol.TType.I64, (short)1);
  private static final org.apache.thrift.protocol.TField IP_HOST_FIELD_DESC = new org.apache.thrift.protocol.TField("ipHost", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField PORT_FIELD_DESC = new org.apache.thrift.protocol.TField("port", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField SECURE_FIELD_DESC = new org.apache.thrift.protocol.TField("secure", org.apache.thrift.protocol.TType.BOOL, (short)4);
  private static final org.apache.thrift.protocol.TField VERIFY_CERTIFICATE_FIELD_DESC = new org.apache.thrift.protocol.TField("verifyCertificate", org.apache.thrift.protocol.TType.BOOL, (short)5);
  private static final org.apache.thrift.protocol.TField DATA_WAIT_FIELD_DESC = new org.apache.thrift.protocol.TField("dataWait", org.apache.thrift.protocol.TType.I32, (short)6);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new ProxyOpenParamStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new ProxyOpenParamTupleSchemeFactory();

  public long handle; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String ipHost; // required
  public int port; // required
  public boolean secure; // required
  public boolean verifyCertificate; // required
  public int dataWait; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    HANDLE((short)1, "handle"),
    IP_HOST((short)2, "ipHost"),
    PORT((short)3, "port"),
    SECURE((short)4, "secure"),
    VERIFY_CERTIFICATE((short)5, "verifyCertificate"),
    DATA_WAIT((short)6, "dataWait");

    private static final java.util.Map<java.lang.String, _Fields> byName = new java.util.HashMap<java.lang.String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // HANDLE
          return HANDLE;
        case 2: // IP_HOST
          return IP_HOST;
        case 3: // PORT
          return PORT;
        case 4: // SECURE
          return SECURE;
        case 5: // VERIFY_CERTIFICATE
          return VERIFY_CERTIFICATE;
        case 6: // DATA_WAIT
          return DATA_WAIT;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new java.lang.IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    @org.apache.thrift.annotation.Nullable
    public static _Fields findByName(java.lang.String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final java.lang.String _fieldName;

    _Fields(short thriftId, java.lang.String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public java.lang.String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __HANDLE_ISSET_ID = 0;
  private static final int __PORT_ISSET_ID = 1;
  private static final int __SECURE_ISSET_ID = 2;
  private static final int __VERIFYCERTIFICATE_ISSET_ID = 3;
  private static final int __DATAWAIT_ISSET_ID = 4;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.HANDLE, new org.apache.thrift.meta_data.FieldMetaData("handle", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.IP_HOST, new org.apache.thrift.meta_data.FieldMetaData("ipHost", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.PORT, new org.apache.thrift.meta_data.FieldMetaData("port", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.SECURE, new org.apache.thrift.meta_data.FieldMetaData("secure", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.VERIFY_CERTIFICATE, new org.apache.thrift.meta_data.FieldMetaData("verifyCertificate", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.BOOL)));
    tmpMap.put(_Fields.DATA_WAIT, new org.apache.thrift.meta_data.FieldMetaData("dataWait", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ProxyOpenParam.class, metaDataMap);
  }

  public ProxyOpenParam() {
  }

  public ProxyOpenParam(
    long handle,
    java.lang.String ipHost,
    int port,
    boolean secure,
    boolean verifyCertificate,
    int dataWait)
  {
    this();
    this.handle = handle;
    setHandleIsSet(true);
    this.ipHost = ipHost;
    this.port = port;
    setPortIsSet(true);
    this.secure = secure;
    setSecureIsSet(true);
    this.verifyCertificate = verifyCertificate;
    setVerifyCertificateIsSet(true);
    this.dataWait = dataWait;
    setDataWaitIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ProxyOpenParam(ProxyOpenParam other) {
    __isset_bitfield = other.__isset_bitfield;
    this.handle = other.handle;
    if (other.isSetIpHost()) {
      this.ipHost = other.ipHost;
    }
    this.port = other.port;
    this.secure = other.secure;
    this.verifyCertificate = other.verifyCertificate;
    this.dataWait = other.dataWait;
  }

  public ProxyOpenParam deepCopy() {
    return new ProxyOpenParam(this);
  }

  @Override
  public void clear() {
    setHandleIsSet(false);
    this.handle = 0;
    this.ipHost = null;
    setPortIsSet(false);
    this.port = 0;
    setSecureIsSet(false);
    this.secure = false;
    setVerifyCertificateIsSet(false);
    this.verifyCertificate = false;
    setDataWaitIsSet(false);
    this.dataWait = 0;
  }

  public long getHandle() {
    return this.handle;
  }

  public ProxyOpenParam setHandle(long handle) {
    this.handle = handle;
    setHandleIsSet(true);
    return this;
  }

  public void unsetHandle() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __HANDLE_ISSET_ID);
  }

  /** Returns true if field handle is set (has been assigned a value) and false otherwise */
  public boolean isSetHandle() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __HANDLE_ISSET_ID);
  }

  public void setHandleIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __HANDLE_ISSET_ID, value);
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getIpHost() {
    return this.ipHost;
  }

  public ProxyOpenParam setIpHost(@org.apache.thrift.annotation.Nullable java.lang.String ipHost) {
    this.ipHost = ipHost;
    return this;
  }

  public void unsetIpHost() {
    this.ipHost = null;
  }

  /** Returns true if field ipHost is set (has been assigned a value) and false otherwise */
  public boolean isSetIpHost() {
    return this.ipHost != null;
  }

  public void setIpHostIsSet(boolean value) {
    if (!value) {
      this.ipHost = null;
    }
  }

  public int getPort() {
    return this.port;
  }

  public ProxyOpenParam setPort(int port) {
    this.port = port;
    setPortIsSet(true);
    return this;
  }

  public void unsetPort() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __PORT_ISSET_ID);
  }

  /** Returns true if field port is set (has been assigned a value) and false otherwise */
  public boolean isSetPort() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __PORT_ISSET_ID);
  }

  public void setPortIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __PORT_ISSET_ID, value);
  }

  public boolean isSecure() {
    return this.secure;
  }

  public ProxyOpenParam setSecure(boolean secure) {
    this.secure = secure;
    setSecureIsSet(true);
    return this;
  }

  public void unsetSecure() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __SECURE_ISSET_ID);
  }

  /** Returns true if field secure is set (has been assigned a value) and false otherwise */
  public boolean isSetSecure() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __SECURE_ISSET_ID);
  }

  public void setSecureIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __SECURE_ISSET_ID, value);
  }

  public boolean isVerifyCertificate() {
    return this.verifyCertificate;
  }

  public ProxyOpenParam setVerifyCertificate(boolean verifyCertificate) {
    this.verifyCertificate = verifyCertificate;
    setVerifyCertificateIsSet(true);
    return this;
  }

  public void unsetVerifyCertificate() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __VERIFYCERTIFICATE_ISSET_ID);
  }

  /** Returns true if field verifyCertificate is set (has been assigned a value) and false otherwise */
  public boolean isSetVerifyCertificate() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __VERIFYCERTIFICATE_ISSET_ID);
  }

  public void setVerifyCertificateIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __VERIFYCERTIFICATE_ISSET_ID, value);
  }

  public int getDataWait() {
    return this.dataWait;
  }

  public ProxyOpenParam setDataWait(int dataWait) {
    this.dataWait = dataWait;
    setDataWaitIsSet(true);
    return this;
  }

  public void unsetDataWait() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __DATAWAIT_ISSET_ID);
  }

  /** Returns true if field dataWait is set (has been assigned a value) and false otherwise */
  public boolean isSetDataWait() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __DATAWAIT_ISSET_ID);
  }

  public void setDataWaitIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __DATAWAIT_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case HANDLE:
      if (value == null) {
        unsetHandle();
      } else {
        setHandle((java.lang.Long)value);
      }
      break;

    case IP_HOST:
      if (value == null) {
        unsetIpHost();
      } else {
        setIpHost((java.lang.String)value);
      }
      break;

    case PORT:
      if (value == null) {
        unsetPort();
      } else {
        setPort((java.lang.Integer)value);
      }
      break;

    case SECURE:
      if (value == null) {
        unsetSecure();
      } else {
        setSecure((java.lang.Boolean)value);
      }
      break;

    case VERIFY_CERTIFICATE:
      if (value == null) {
        unsetVerifyCertificate();
      } else {
        setVerifyCertificate((java.lang.Boolean)value);
      }
      break;

    case DATA_WAIT:
      if (value == null) {
        unsetDataWait();
      } else {
        setDataWait((java.lang.Integer)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case HANDLE:
      return getHandle();

    case IP_HOST:
      return getIpHost();

    case PORT:
      return getPort();

    case SECURE:
      return isSecure();

    case VERIFY_CERTIFICATE:
      return isVerifyCertificate();

    case DATA_WAIT:
      return getDataWait();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case HANDLE:
      return isSetHandle();
    case IP_HOST:
      return isSetIpHost();
    case PORT:
      return isSetPort();
    case SECURE:
      return isSetSecure();
    case VERIFY_CERTIFICATE:
      return isSetVerifyCertificate();
    case DATA_WAIT:
      return isSetDataWait();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof ProxyOpenParam)
      return this.equals((ProxyOpenParam)that);
    return false;
  }

  public boolean equals(ProxyOpenParam that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_handle = true;
    boolean that_present_handle = true;
    if (this_present_handle || that_present_handle) {
      if (!(this_present_handle && that_present_handle))
        return false;
      if (this.handle != that.handle)
        return false;
    }

    boolean this_present_ipHost = true && this.isSetIpHost();
    boolean that_present_ipHost = true && that.isSetIpHost();
    if (this_present_ipHost || that_present_ipHost) {
      if (!(this_present_ipHost && that_present_ipHost))
        return false;
      if (!this.ipHost.equals(that.ipHost))
        return false;
    }

    boolean this_present_port = true;
    boolean that_present_port = true;
    if (this_present_port || that_present_port) {
      if (!(this_present_port && that_present_port))
        return false;
      if (this.port != that.port)
        return false;
    }

    boolean this_present_secure = true;
    boolean that_present_secure = true;
    if (this_present_secure || that_present_secure) {
      if (!(this_present_secure && that_present_secure))
        return false;
      if (this.secure != that.secure)
        return false;
    }

    boolean this_present_verifyCertificate = true;
    boolean that_present_verifyCertificate = true;
    if (this_present_verifyCertificate || that_present_verifyCertificate) {
      if (!(this_present_verifyCertificate && that_present_verifyCertificate))
        return false;
      if (this.verifyCertificate != that.verifyCertificate)
        return false;
    }

    boolean this_present_dataWait = true;
    boolean that_present_dataWait = true;
    if (this_present_dataWait || that_present_dataWait) {
      if (!(this_present_dataWait && that_present_dataWait))
        return false;
      if (this.dataWait != that.dataWait)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(handle);

    hashCode = hashCode * 8191 + ((isSetIpHost()) ? 131071 : 524287);
    if (isSetIpHost())
      hashCode = hashCode * 8191 + ipHost.hashCode();

    hashCode = hashCode * 8191 + port;

    hashCode = hashCode * 8191 + ((secure) ? 131071 : 524287);

    hashCode = hashCode * 8191 + ((verifyCertificate) ? 131071 : 524287);

    hashCode = hashCode * 8191 + dataWait;

    return hashCode;
  }

  @Override
  public int compareTo(ProxyOpenParam other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetHandle()).compareTo(other.isSetHandle());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetHandle()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.handle, other.handle);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetIpHost()).compareTo(other.isSetIpHost());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIpHost()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ipHost, other.ipHost);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetPort()).compareTo(other.isSetPort());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetPort()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.port, other.port);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetSecure()).compareTo(other.isSetSecure());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSecure()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.secure, other.secure);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetVerifyCertificate()).compareTo(other.isSetVerifyCertificate());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetVerifyCertificate()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.verifyCertificate, other.verifyCertificate);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetDataWait()).compareTo(other.isSetDataWait());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDataWait()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.dataWait, other.dataWait);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  @org.apache.thrift.annotation.Nullable
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }

  @Override
  public java.lang.String toString() {
    java.lang.StringBuilder sb = new java.lang.StringBuilder("ProxyOpenParam(");
    boolean first = true;

    sb.append("handle:");
    sb.append(this.handle);
    first = false;
    if (!first) sb.append(", ");
    sb.append("ipHost:");
    if (this.ipHost == null) {
      sb.append("null");
    } else {
      sb.append(this.ipHost);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("port:");
    sb.append(this.port);
    first = false;
    if (!first) sb.append(", ");
    sb.append("secure:");
    sb.append(this.secure);
    first = false;
    if (!first) sb.append(", ");
    sb.append("verifyCertificate:");
    sb.append(this.verifyCertificate);
    first = false;
    if (!first) sb.append(", ");
    sb.append("dataWait:");
    sb.append(this.dataWait);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, java.lang.ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ProxyOpenParamStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public ProxyOpenParamStandardScheme getScheme() {
      return new ProxyOpenParamStandardScheme();
    }
  }

  private static class ProxyOpenParamStandardScheme extends org.apache.thrift.scheme.StandardScheme<ProxyOpenParam> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ProxyOpenParam struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // HANDLE
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.handle = iprot.readI64();
              struct.setHandleIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // IP_HOST
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.ipHost = iprot.readString();
              struct.setIpHostIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // PORT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.port = iprot.readI32();
              struct.setPortIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // SECURE
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.secure = iprot.readBool();
              struct.setSecureIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // VERIFY_CERTIFICATE
            if (schemeField.type == org.apache.thrift.protocol.TType.BOOL) {
              struct.verifyCertificate = iprot.readBool();
              struct.setVerifyCertificateIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // DATA_WAIT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.dataWait = iprot.readI32();
              struct.setDataWaitIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ProxyOpenParam struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(HANDLE_FIELD_DESC);
      oprot.writeI64(struct.handle);
      oprot.writeFieldEnd();
      if (struct.ipHost != null) {
        oprot.writeFieldBegin(IP_HOST_FIELD_DESC);
        oprot.writeString(struct.ipHost);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(PORT_FIELD_DESC);
      oprot.writeI32(struct.port);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(SECURE_FIELD_DESC);
      oprot.writeBool(struct.secure);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(VERIFY_CERTIFICATE_FIELD_DESC);
      oprot.writeBool(struct.verifyCertificate);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(DATA_WAIT_FIELD_DESC);
      oprot.writeI32(struct.dataWait);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ProxyOpenParamTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public ProxyOpenParamTupleScheme getScheme() {
      return new ProxyOpenParamTupleScheme();
    }
  }

  private static class ProxyOpenParamTupleScheme extends org.apache.thrift.scheme.TupleScheme<ProxyOpenParam> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ProxyOpenParam struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetHandle()) {
        optionals.set(0);
      }
      if (struct.isSetIpHost()) {
        optionals.set(1);
      }
      if (struct.isSetPort()) {
        optionals.set(2);
      }
      if (struct.isSetSecure()) {
        optionals.set(3);
      }
      if (struct.isSetVerifyCertificate()) {
        optionals.set(4);
      }
      if (struct.isSetDataWait()) {
        optionals.set(5);
      }
      oprot.writeBitSet(optionals, 6);
      if (struct.isSetHandle()) {
        oprot.writeI64(struct.handle);
      }
      if (struct.isSetIpHost()) {
        oprot.writeString(struct.ipHost);
      }
      if (struct.isSetPort()) {
        oprot.writeI32(struct.port);
      }
      if (struct.isSetSecure()) {
        oprot.writeBool(struct.secure);
      }
      if (struct.isSetVerifyCertificate()) {
        oprot.writeBool(struct.verifyCertificate);
      }
      if (struct.isSetDataWait()) {
        oprot.writeI32(struct.dataWait);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ProxyOpenParam struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(6);
      if (incoming.get(0)) {
        struct.handle = iprot.readI64();
        struct.setHandleIsSet(true);
      }
      if (incoming.get(1)) {
        struct.ipHost = iprot.readString();
        struct.setIpHostIsSet(true);
      }
      if (incoming.get(2)) {
        struct.port = iprot.readI32();
        struct.setPortIsSet(true);
      }
      if (incoming.get(3)) {
        struct.secure = iprot.readBool();
        struct.setSecureIsSet(true);
      }
      if (incoming.get(4)) {
        struct.verifyCertificate = iprot.readBool();
        struct.setVerifyCertificateIsSet(true);
      }
      if (incoming.get(5)) {
        struct.dataWait = iprot.readI32();
        struct.setDataWaitIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}


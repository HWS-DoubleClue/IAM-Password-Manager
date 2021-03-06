/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.doubleclue.comm.thrift;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.13.0)", date = "2021-10-21")
public class QrCodeResponse implements org.apache.thrift.TBase<QrCodeResponse, QrCodeResponse._Fields>, java.io.Serializable, Cloneable, Comparable<QrCodeResponse> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("QrCodeResponse");

  private static final org.apache.thrift.protocol.TField TIME_TO_LIVE_FIELD_DESC = new org.apache.thrift.protocol.TField("timeToLive", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("data", org.apache.thrift.protocol.TType.STRING, (short)2);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new QrCodeResponseStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new QrCodeResponseTupleSchemeFactory();

  public int timeToLive; // required
  public @org.apache.thrift.annotation.Nullable java.lang.String data; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    TIME_TO_LIVE((short)1, "timeToLive"),
    DATA((short)2, "data");

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
        case 1: // TIME_TO_LIVE
          return TIME_TO_LIVE;
        case 2: // DATA
          return DATA;
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
  private static final int __TIMETOLIVE_ISSET_ID = 0;
  private byte __isset_bitfield = 0;
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.TIME_TO_LIVE, new org.apache.thrift.meta_data.FieldMetaData("timeToLive", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.DATA, new org.apache.thrift.meta_data.FieldMetaData("data", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(QrCodeResponse.class, metaDataMap);
  }

  public QrCodeResponse() {
  }

  public QrCodeResponse(
    int timeToLive,
    java.lang.String data)
  {
    this();
    this.timeToLive = timeToLive;
    setTimeToLiveIsSet(true);
    this.data = data;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public QrCodeResponse(QrCodeResponse other) {
    __isset_bitfield = other.__isset_bitfield;
    this.timeToLive = other.timeToLive;
    if (other.isSetData()) {
      this.data = other.data;
    }
  }

  public QrCodeResponse deepCopy() {
    return new QrCodeResponse(this);
  }

  @Override
  public void clear() {
    setTimeToLiveIsSet(false);
    this.timeToLive = 0;
    this.data = null;
  }

  public int getTimeToLive() {
    return this.timeToLive;
  }

  public QrCodeResponse setTimeToLive(int timeToLive) {
    this.timeToLive = timeToLive;
    setTimeToLiveIsSet(true);
    return this;
  }

  public void unsetTimeToLive() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __TIMETOLIVE_ISSET_ID);
  }

  /** Returns true if field timeToLive is set (has been assigned a value) and false otherwise */
  public boolean isSetTimeToLive() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __TIMETOLIVE_ISSET_ID);
  }

  public void setTimeToLiveIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __TIMETOLIVE_ISSET_ID, value);
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.String getData() {
    return this.data;
  }

  public QrCodeResponse setData(@org.apache.thrift.annotation.Nullable java.lang.String data) {
    this.data = data;
    return this;
  }

  public void unsetData() {
    this.data = null;
  }

  /** Returns true if field data is set (has been assigned a value) and false otherwise */
  public boolean isSetData() {
    return this.data != null;
  }

  public void setDataIsSet(boolean value) {
    if (!value) {
      this.data = null;
    }
  }

  public void setFieldValue(_Fields field, @org.apache.thrift.annotation.Nullable java.lang.Object value) {
    switch (field) {
    case TIME_TO_LIVE:
      if (value == null) {
        unsetTimeToLive();
      } else {
        setTimeToLive((java.lang.Integer)value);
      }
      break;

    case DATA:
      if (value == null) {
        unsetData();
      } else {
        setData((java.lang.String)value);
      }
      break;

    }
  }

  @org.apache.thrift.annotation.Nullable
  public java.lang.Object getFieldValue(_Fields field) {
    switch (field) {
    case TIME_TO_LIVE:
      return getTimeToLive();

    case DATA:
      return getData();

    }
    throw new java.lang.IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new java.lang.IllegalArgumentException();
    }

    switch (field) {
    case TIME_TO_LIVE:
      return isSetTimeToLive();
    case DATA:
      return isSetData();
    }
    throw new java.lang.IllegalStateException();
  }

  @Override
  public boolean equals(java.lang.Object that) {
    if (that == null)
      return false;
    if (that instanceof QrCodeResponse)
      return this.equals((QrCodeResponse)that);
    return false;
  }

  public boolean equals(QrCodeResponse that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_timeToLive = true;
    boolean that_present_timeToLive = true;
    if (this_present_timeToLive || that_present_timeToLive) {
      if (!(this_present_timeToLive && that_present_timeToLive))
        return false;
      if (this.timeToLive != that.timeToLive)
        return false;
    }

    boolean this_present_data = true && this.isSetData();
    boolean that_present_data = true && that.isSetData();
    if (this_present_data || that_present_data) {
      if (!(this_present_data && that_present_data))
        return false;
      if (!this.data.equals(that.data))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + timeToLive;

    hashCode = hashCode * 8191 + ((isSetData()) ? 131071 : 524287);
    if (isSetData())
      hashCode = hashCode * 8191 + data.hashCode();

    return hashCode;
  }

  @Override
  public int compareTo(QrCodeResponse other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = java.lang.Boolean.valueOf(isSetTimeToLive()).compareTo(other.isSetTimeToLive());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetTimeToLive()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.timeToLive, other.timeToLive);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = java.lang.Boolean.valueOf(isSetData()).compareTo(other.isSetData());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetData()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.data, other.data);
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
    java.lang.StringBuilder sb = new java.lang.StringBuilder("QrCodeResponse(");
    boolean first = true;

    sb.append("timeToLive:");
    sb.append(this.timeToLive);
    first = false;
    if (!first) sb.append(", ");
    sb.append("data:");
    if (this.data == null) {
      sb.append("null");
    } else {
      sb.append(this.data);
    }
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

  private static class QrCodeResponseStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public QrCodeResponseStandardScheme getScheme() {
      return new QrCodeResponseStandardScheme();
    }
  }

  private static class QrCodeResponseStandardScheme extends org.apache.thrift.scheme.StandardScheme<QrCodeResponse> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, QrCodeResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // TIME_TO_LIVE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.timeToLive = iprot.readI32();
              struct.setTimeToLiveIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // DATA
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.data = iprot.readString();
              struct.setDataIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, QrCodeResponse struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(TIME_TO_LIVE_FIELD_DESC);
      oprot.writeI32(struct.timeToLive);
      oprot.writeFieldEnd();
      if (struct.data != null) {
        oprot.writeFieldBegin(DATA_FIELD_DESC);
        oprot.writeString(struct.data);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class QrCodeResponseTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public QrCodeResponseTupleScheme getScheme() {
      return new QrCodeResponseTupleScheme();
    }
  }

  private static class QrCodeResponseTupleScheme extends org.apache.thrift.scheme.TupleScheme<QrCodeResponse> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, QrCodeResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetTimeToLive()) {
        optionals.set(0);
      }
      if (struct.isSetData()) {
        optionals.set(1);
      }
      oprot.writeBitSet(optionals, 2);
      if (struct.isSetTimeToLive()) {
        oprot.writeI32(struct.timeToLive);
      }
      if (struct.isSetData()) {
        oprot.writeString(struct.data);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, QrCodeResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(2);
      if (incoming.get(0)) {
        struct.timeToLive = iprot.readI32();
        struct.setTimeToLiveIsSet(true);
      }
      if (incoming.get(1)) {
        struct.data = iprot.readString();
        struct.setDataIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}


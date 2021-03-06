/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

import Foundation

import Thrift


fileprivate final class ServerToApp_disconnect_args {


  fileprivate init() { }
}

fileprivate func ==(lhs: ServerToApp_disconnect_args, rhs: ServerToApp_disconnect_args) -> Bool {
  return true
}

extension ServerToApp_disconnect_args : Hashable {

  fileprivate var hashValue : Int {
    return 31
  }

}

extension ServerToApp_disconnect_args : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return [:]
  }

  fileprivate static var structName: String { return "ServerToApp_disconnect_args" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_disconnect_args {
    _ = try proto.readStructBegin()

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()

    return ServerToApp_disconnect_args()
  }

}



fileprivate final class ServerToApp_disconnect_result {

  fileprivate var ouch: AppException?


  fileprivate init() { }
  fileprivate init(ouch: AppException?) {
    self.ouch = ouch
  }

}

fileprivate func ==(lhs: ServerToApp_disconnect_result, rhs: ServerToApp_disconnect_result) -> Bool {
  return
    (lhs.ouch == rhs.ouch)
}

extension ServerToApp_disconnect_result : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (ouch?.hashValue ?? 0)
    return result
  }

}

extension ServerToApp_disconnect_result : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["ouch": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_disconnect_result" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_disconnect_result {
    _ = try proto.readStructBegin()
    var ouch: AppException?

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (1, .struct):           ouch = try AppException.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()

    return ServerToApp_disconnect_result(ouch: ouch)
  }

}



fileprivate final class ServerToApp_sendMessage_args {

  fileprivate var appMessage: AppMessage


  fileprivate init(appMessage: AppMessage) {
    self.appMessage = appMessage
  }

}

fileprivate func ==(lhs: ServerToApp_sendMessage_args, rhs: ServerToApp_sendMessage_args) -> Bool {
  return
    (lhs.appMessage == rhs.appMessage)
}

extension ServerToApp_sendMessage_args : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (appMessage.hashValue)
    return result
  }

}

extension ServerToApp_sendMessage_args : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["appMessage": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_sendMessage_args" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_sendMessage_args {
    _ = try proto.readStructBegin()
    var appMessage: AppMessage!

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (1, .struct):           appMessage = try AppMessage.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()
    // Required fields
    try proto.validateValue(appMessage, named: "appMessage")

    return ServerToApp_sendMessage_args(appMessage: appMessage)
  }

}



fileprivate final class ServerToApp_sendMessage_result {

  fileprivate var success: Bool?

  fileprivate var ouch: AppException?


  fileprivate init() { }
  fileprivate init(success: Bool?, ouch: AppException?) {
    self.success = success
    self.ouch = ouch
  }

}

fileprivate func ==(lhs: ServerToApp_sendMessage_result, rhs: ServerToApp_sendMessage_result) -> Bool {
  return
    (lhs.success == rhs.success) &&
    (lhs.ouch == rhs.ouch)
}

extension ServerToApp_sendMessage_result : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (success?.hashValue ?? 0)
    result = prime &* result &+ (ouch?.hashValue ?? 0)
    return result
  }

}

extension ServerToApp_sendMessage_result : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["success": 0, "ouch": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_sendMessage_result" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_sendMessage_result {
    _ = try proto.readStructBegin()
    var success: Bool?
    var ouch: AppException?

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (0, .bool):            success = try Bool.read(from: proto)
        case (1, .struct):           ouch = try AppException.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()

    return ServerToApp_sendMessage_result(success: success, ouch: ouch)
  }

}



fileprivate final class ServerToApp_authAppMessageResponse_args {

  fileprivate var authAppMessageResponse: AuthAppMessageResponse


  fileprivate init(authAppMessageResponse: AuthAppMessageResponse) {
    self.authAppMessageResponse = authAppMessageResponse
  }

}

fileprivate func ==(lhs: ServerToApp_authAppMessageResponse_args, rhs: ServerToApp_authAppMessageResponse_args) -> Bool {
  return
    (lhs.authAppMessageResponse == rhs.authAppMessageResponse)
}

extension ServerToApp_authAppMessageResponse_args : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (authAppMessageResponse.hashValue)
    return result
  }

}

extension ServerToApp_authAppMessageResponse_args : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["authAppMessageResponse": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_authAppMessageResponse_args" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_authAppMessageResponse_args {
    _ = try proto.readStructBegin()
    var authAppMessageResponse: AuthAppMessageResponse!

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (1, .struct):           authAppMessageResponse = try AuthAppMessageResponse.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()
    // Required fields
    try proto.validateValue(authAppMessageResponse, named: "authAppMessageResponse")

    return ServerToApp_authAppMessageResponse_args(authAppMessageResponse: authAppMessageResponse)
  }

}



fileprivate final class ServerToApp_authAppMessageResponse_result {

  fileprivate var ouch: AppException?


  fileprivate init() { }
  fileprivate init(ouch: AppException?) {
    self.ouch = ouch
  }

}

fileprivate func ==(lhs: ServerToApp_authAppMessageResponse_result, rhs: ServerToApp_authAppMessageResponse_result) -> Bool {
  return
    (lhs.ouch == rhs.ouch)
}

extension ServerToApp_authAppMessageResponse_result : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (ouch?.hashValue ?? 0)
    return result
  }

}

extension ServerToApp_authAppMessageResponse_result : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["ouch": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_authAppMessageResponse_result" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_authAppMessageResponse_result {
    _ = try proto.readStructBegin()
    var ouch: AppException?

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (1, .struct):           ouch = try AppException.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()

    return ServerToApp_authAppMessageResponse_result(ouch: ouch)
  }

}



fileprivate final class ServerToApp_proxyOpen_args {

  fileprivate var proxyOpenParam: ProxyOpenParam


  fileprivate init(proxyOpenParam: ProxyOpenParam) {
    self.proxyOpenParam = proxyOpenParam
  }

}

fileprivate func ==(lhs: ServerToApp_proxyOpen_args, rhs: ServerToApp_proxyOpen_args) -> Bool {
  return
    (lhs.proxyOpenParam == rhs.proxyOpenParam)
}

extension ServerToApp_proxyOpen_args : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (proxyOpenParam.hashValue)
    return result
  }

}

extension ServerToApp_proxyOpen_args : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["proxyOpenParam": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_proxyOpen_args" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_proxyOpen_args {
    _ = try proto.readStructBegin()
    var proxyOpenParam: ProxyOpenParam!

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (1, .struct):           proxyOpenParam = try ProxyOpenParam.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()
    // Required fields
    try proto.validateValue(proxyOpenParam, named: "proxyOpenParam")

    return ServerToApp_proxyOpen_args(proxyOpenParam: proxyOpenParam)
  }

}



fileprivate final class ServerToApp_proxyOpen_result {

  fileprivate var ouch: AppException?


  fileprivate init() { }
  fileprivate init(ouch: AppException?) {
    self.ouch = ouch
  }

}

fileprivate func ==(lhs: ServerToApp_proxyOpen_result, rhs: ServerToApp_proxyOpen_result) -> Bool {
  return
    (lhs.ouch == rhs.ouch)
}

extension ServerToApp_proxyOpen_result : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (ouch?.hashValue ?? 0)
    return result
  }

}

extension ServerToApp_proxyOpen_result : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["ouch": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_proxyOpen_result" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_proxyOpen_result {
    _ = try proto.readStructBegin()
    var ouch: AppException?

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (1, .struct):           ouch = try AppException.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()

    return ServerToApp_proxyOpen_result(ouch: ouch)
  }

}



fileprivate final class ServerToApp_proxyData_args {

  fileprivate var handle: Int64

  fileprivate var data: Data


  fileprivate init(handle: Int64, data: Data) {
    self.handle = handle
    self.data = data
  }

}

fileprivate func ==(lhs: ServerToApp_proxyData_args, rhs: ServerToApp_proxyData_args) -> Bool {
  return
    (lhs.handle == rhs.handle) &&
    (lhs.data == rhs.data)
}

extension ServerToApp_proxyData_args : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (handle.hashValue)
    result = prime &* result &+ (data.hashValue)
    return result
  }

}

extension ServerToApp_proxyData_args : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["handle": 1, "data": 2, ]
  }

  fileprivate static var structName: String { return "ServerToApp_proxyData_args" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_proxyData_args {
    _ = try proto.readStructBegin()
    var handle: Int64!
    var data: Data!

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (1, .i64):             handle = try Int64.read(from: proto)
        case (2, .string):           data = try Data.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()
    // Required fields
    try proto.validateValue(handle, named: "handle")
    try proto.validateValue(data, named: "data")

    return ServerToApp_proxyData_args(handle: handle, data: data)
  }

}



fileprivate final class ServerToApp_proxyData_result {

  fileprivate var success: Data?

  fileprivate var ouch: AppException?


  fileprivate init() { }
  fileprivate init(success: Data?, ouch: AppException?) {
    self.success = success
    self.ouch = ouch
  }

}

fileprivate func ==(lhs: ServerToApp_proxyData_result, rhs: ServerToApp_proxyData_result) -> Bool {
  return
    (lhs.success == rhs.success) &&
    (lhs.ouch == rhs.ouch)
}

extension ServerToApp_proxyData_result : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (success?.hashValue ?? 0)
    result = prime &* result &+ (ouch?.hashValue ?? 0)
    return result
  }

}

extension ServerToApp_proxyData_result : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["success": 0, "ouch": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_proxyData_result" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_proxyData_result {
    _ = try proto.readStructBegin()
    var success: Data?
    var ouch: AppException?

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (0, .string):           success = try Data.read(from: proto)
        case (1, .struct):           ouch = try AppException.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()

    return ServerToApp_proxyData_result(success: success, ouch: ouch)
  }

}



fileprivate final class ServerToApp_proxyClose_args {

  fileprivate var handle: Int64


  fileprivate init(handle: Int64) {
    self.handle = handle
  }

}

fileprivate func ==(lhs: ServerToApp_proxyClose_args, rhs: ServerToApp_proxyClose_args) -> Bool {
  return
    (lhs.handle == rhs.handle)
}

extension ServerToApp_proxyClose_args : Hashable {

  fileprivate var hashValue : Int {
    let prime = 31
    var result = 1
    result = prime &* result &+ (handle.hashValue)
    return result
  }

}

extension ServerToApp_proxyClose_args : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return ["handle": 1, ]
  }

  fileprivate static var structName: String { return "ServerToApp_proxyClose_args" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_proxyClose_args {
    _ = try proto.readStructBegin()
    var handle: Int64!

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case (1, .i64):             handle = try Int64.read(from: proto)
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()
    // Required fields
    try proto.validateValue(handle, named: "handle")

    return ServerToApp_proxyClose_args(handle: handle)
  }

}



fileprivate final class ServerToApp_proxyClose_result {


  fileprivate init() { }
}

fileprivate func ==(lhs: ServerToApp_proxyClose_result, rhs: ServerToApp_proxyClose_result) -> Bool {
  return true
}

extension ServerToApp_proxyClose_result : Hashable {

  fileprivate var hashValue : Int {
    return 31
  }

}

extension ServerToApp_proxyClose_result : TStruct {

  fileprivate static var fieldIds: [String: Int32] {
    return [:]
  }

  fileprivate static var structName: String { return "ServerToApp_proxyClose_result" }

  fileprivate static func read(from proto: TProtocol) throws -> ServerToApp_proxyClose_result {
    _ = try proto.readStructBegin()

    fields: while true {

      let (_, fieldType, fieldID) = try proto.readFieldBegin()

      switch (fieldID, fieldType) {
        case (_, .stop):            break fields
        case let (_, unknownType):  try proto.skip(type: unknownType)
      }

      try proto.readFieldEnd()
    }

    try proto.readStructEnd()

    return ServerToApp_proxyClose_result()
  }

}



extension ServerToAppClient : ServerToApp {

  private func send_disconnect() throws {
    try outProtocol.writeMessageBegin(name: "disconnect", type: .call, sequenceID: 0)
    let args = ServerToApp_disconnect_args()
    try args.write(to: outProtocol)
    try outProtocol.writeMessageEnd()
  }

  private func recv_disconnect() throws {
    try inProtocol.readResultMessageBegin() 
    let result = try ServerToApp_disconnect_result.read(from: inProtocol)
    try inProtocol.readMessageEnd()

    if let ouch = result.ouch {
      throw ouch
    }
  }

  public func disconnect() throws {
    try send_disconnect()
    try outProtocol.transport.flush()
    try recv_disconnect()
  }

  private func send_sendMessage(appMessage: AppMessage) throws {
    try outProtocol.writeMessageBegin(name: "sendMessage", type: .call, sequenceID: 0)
    let args = ServerToApp_sendMessage_args(appMessage: appMessage)
    try args.write(to: outProtocol)
    try outProtocol.writeMessageEnd()
  }

  private func recv_sendMessage() throws -> Bool {
    try inProtocol.readResultMessageBegin() 
    let result = try ServerToApp_sendMessage_result.read(from: inProtocol)
    try inProtocol.readMessageEnd()

    if let success = result.success {
      return success
    }
    if let ouch = result.ouch {
      throw ouch
    }
    throw TApplicationError(error: .missingResult(methodName: "sendMessage"))
  }

  public func sendMessage(appMessage: AppMessage) throws -> Bool {
    try send_sendMessage(appMessage: appMessage)
    try outProtocol.transport.flush()
    return try recv_sendMessage()
  }

  private func send_authAppMessageResponse(authAppMessageResponse: AuthAppMessageResponse) throws {
    try outProtocol.writeMessageBegin(name: "authAppMessageResponse", type: .call, sequenceID: 0)
    let args = ServerToApp_authAppMessageResponse_args(authAppMessageResponse: authAppMessageResponse)
    try args.write(to: outProtocol)
    try outProtocol.writeMessageEnd()
  }

  private func recv_authAppMessageResponse() throws {
    try inProtocol.readResultMessageBegin() 
    let result = try ServerToApp_authAppMessageResponse_result.read(from: inProtocol)
    try inProtocol.readMessageEnd()

    if let ouch = result.ouch {
      throw ouch
    }
  }

  public func authAppMessageResponse(authAppMessageResponse: AuthAppMessageResponse) throws {
    try send_authAppMessageResponse(authAppMessageResponse: authAppMessageResponse)
    try outProtocol.transport.flush()
    try recv_authAppMessageResponse()
  }

  private func send_proxyOpen(proxyOpenParam: ProxyOpenParam) throws {
    try outProtocol.writeMessageBegin(name: "proxyOpen", type: .call, sequenceID: 0)
    let args = ServerToApp_proxyOpen_args(proxyOpenParam: proxyOpenParam)
    try args.write(to: outProtocol)
    try outProtocol.writeMessageEnd()
  }

  private func recv_proxyOpen() throws {
    try inProtocol.readResultMessageBegin() 
    let result = try ServerToApp_proxyOpen_result.read(from: inProtocol)
    try inProtocol.readMessageEnd()

    if let ouch = result.ouch {
      throw ouch
    }
  }

  public func proxyOpen(proxyOpenParam: ProxyOpenParam) throws {
    try send_proxyOpen(proxyOpenParam: proxyOpenParam)
    try outProtocol.transport.flush()
    try recv_proxyOpen()
  }

  private func send_proxyData(handle: Int64, data: Data) throws {
    try outProtocol.writeMessageBegin(name: "proxyData", type: .call, sequenceID: 0)
    let args = ServerToApp_proxyData_args(handle: handle, data: data)
    try args.write(to: outProtocol)
    try outProtocol.writeMessageEnd()
  }

  private func recv_proxyData() throws -> Data {
    try inProtocol.readResultMessageBegin() 
    let result = try ServerToApp_proxyData_result.read(from: inProtocol)
    try inProtocol.readMessageEnd()

    if let success = result.success {
      return success
    }
    if let ouch = result.ouch {
      throw ouch
    }
    throw TApplicationError(error: .missingResult(methodName: "proxyData"))
  }

  public func proxyData(handle: Int64, data: Data) throws -> Data {
    try send_proxyData(handle: handle, data: data)
    try outProtocol.transport.flush()
    return try recv_proxyData()
  }

  private func send_proxyClose(handle: Int64) throws {
    try outProtocol.writeMessageBegin(name: "proxyClose", type: .call, sequenceID: 0)
    let args = ServerToApp_proxyClose_args(handle: handle)
    try args.write(to: outProtocol)
    try outProtocol.writeMessageEnd()
  }

  private func recv_proxyClose() throws {
    try inProtocol.readResultMessageBegin() 
    _ = try ServerToApp_proxyClose_result.read(from: inProtocol)
    try inProtocol.readMessageEnd()

  }

  public func proxyClose(handle: Int64) throws {
    try send_proxyClose(handle: handle)
    try outProtocol.transport.flush()
    try recv_proxyClose()
  }

}

extension ServerToAppProcessor : TProcessor {

  static let processorHandlers: ProcessorHandlerDictionary = {

    var processorHandlers = ProcessorHandlerDictionary()

    processorHandlers["disconnect"] = { sequenceID, inProtocol, outProtocol, handler in

      let args = try ServerToApp_disconnect_args.read(from: inProtocol)

      try inProtocol.readMessageEnd()

      var result = ServerToApp_disconnect_result()
      do {
        try handler.disconnect()
      }
      catch let error as AppException { result.ouch = error }
      catch let error { throw error }

      try outProtocol.writeMessageBegin(name: "disconnect", type: .reply, sequenceID: sequenceID)
      try result.write(to: outProtocol)
      try outProtocol.writeMessageEnd()
    }
    processorHandlers["sendMessage"] = { sequenceID, inProtocol, outProtocol, handler in

      let args = try ServerToApp_sendMessage_args.read(from: inProtocol)

      try inProtocol.readMessageEnd()

      var result = ServerToApp_sendMessage_result()
      do {
        result.success = try handler.sendMessage(appMessage: args.appMessage)
      }
      catch let error as AppException { result.ouch = error }
      catch let error { throw error }

      try outProtocol.writeMessageBegin(name: "sendMessage", type: .reply, sequenceID: sequenceID)
      try result.write(to: outProtocol)
      try outProtocol.writeMessageEnd()
    }
    processorHandlers["authAppMessageResponse"] = { sequenceID, inProtocol, outProtocol, handler in

      let args = try ServerToApp_authAppMessageResponse_args.read(from: inProtocol)

      try inProtocol.readMessageEnd()

      var result = ServerToApp_authAppMessageResponse_result()
      do {
        try handler.authAppMessageResponse(authAppMessageResponse: args.authAppMessageResponse)
      }
      catch let error as AppException { result.ouch = error }
      catch let error { throw error }

      try outProtocol.writeMessageBegin(name: "authAppMessageResponse", type: .reply, sequenceID: sequenceID)
      try result.write(to: outProtocol)
      try outProtocol.writeMessageEnd()
    }
    processorHandlers["proxyOpen"] = { sequenceID, inProtocol, outProtocol, handler in

      let args = try ServerToApp_proxyOpen_args.read(from: inProtocol)

      try inProtocol.readMessageEnd()

      var result = ServerToApp_proxyOpen_result()
      do {
        try handler.proxyOpen(proxyOpenParam: args.proxyOpenParam)
      }
      catch let error as AppException { result.ouch = error }
      catch let error { throw error }

      try outProtocol.writeMessageBegin(name: "proxyOpen", type: .reply, sequenceID: sequenceID)
      try result.write(to: outProtocol)
      try outProtocol.writeMessageEnd()
    }
    processorHandlers["proxyData"] = { sequenceID, inProtocol, outProtocol, handler in

      let args = try ServerToApp_proxyData_args.read(from: inProtocol)

      try inProtocol.readMessageEnd()

      var result = ServerToApp_proxyData_result()
      do {
        result.success = try handler.proxyData(handle: args.handle, data: args.data)
      }
      catch let error as AppException { result.ouch = error }
      catch let error { throw error }

      try outProtocol.writeMessageBegin(name: "proxyData", type: .reply, sequenceID: sequenceID)
      try result.write(to: outProtocol)
      try outProtocol.writeMessageEnd()
    }
    processorHandlers["proxyClose"] = { sequenceID, inProtocol, outProtocol, handler in

      let args = try ServerToApp_proxyClose_args.read(from: inProtocol)

      try inProtocol.readMessageEnd()

      var result = ServerToApp_proxyClose_result()
      do {
        try handler.proxyClose(handle: args.handle)
      }
      catch let error { throw error }

      try outProtocol.writeMessageBegin(name: "proxyClose", type: .reply, sequenceID: sequenceID)
      try result.write(to: outProtocol)
      try outProtocol.writeMessageEnd()
    }
    return processorHandlers
  }()

  public func process(on inProtocol: TProtocol, outProtocol: TProtocol) throws {

    let (messageName, _, sequenceID) = try inProtocol.readMessageBegin()

    if let processorHandler = ServerToAppProcessor.processorHandlers[messageName] {
      do {
        try processorHandler(sequenceID, inProtocol, outProtocol, service)
      }
      catch let error as TApplicationError {
        try outProtocol.writeException(messageName: messageName, sequenceID: sequenceID, ex: error)
      }
    }
    else {
      try inProtocol.skip(type: .struct)
      try inProtocol.readMessageEnd()
      let ex = TApplicationError(error: .unknownMethod(methodName: messageName))
      try outProtocol.writeException(messageName: messageName, sequenceID: sequenceID, ex: ex)
    }
  }
}


# Documentation of the Thrift Library
This is the documentation for the ``LibAsThrift`` library which is used for the communication between
DoubleClue Apps and DCEM

## How the Thrift library is generated
The Following docs shows how to build the Thrift library.
 
After compiling the package has to "organise Imports" on the package. You can do that with the 
shortcut ``CTRL+Shift+O`` after you have selected the source directory folder.

## Different Types of Thrift
There are mainly two different types of Thrift:
- ``AppToServer`` is responsible for the communication from the client to the server
- ``ServerToApp`` is responsible for method calls from the server

There are different configurations for generating the Thrift library:
- Java Configuration
- C-Sharp Configuration

Each of these configurations can be for ``AppToServer`` or ``ServerToApp``

For the Following configuration we use two Placeholders:
- **genType**: ``java`` or ``csharp``
- **thriftFile**: ``AppToServer.thrift`` or ``ServerToApp.thrift``

Create a new configuration for external tools in eclipse. For the Arguments field, copy the following and replace the ``{placeholders}``:

- Location: path of your ``thrift-0.9.3.exe`` -> You can find this exe in the ``LibAsThrift`` directory in
the folder ``executable``
- Arguments
  ```
  -r  --gen {genType}
  -o   ${workspace_loc}\LibAsThrift\src\main\{genType}\ 
  -out  ${workspace_loc}\LibAsThrift\src\main\{genType}\ 
  -verbose -I  ${workspace_loc}\LibAsThrift\artifacts 
  ${workspace_loc}\LibAsThrift\artifacts\{thriftFile}
  ```
  
## Organize Import after create Java Source

After createing the Java sources you need to use the eclipse utility to organize the java sources imports.
Then you have to replace "org.slf4j." with "" in classes marked with an error.  

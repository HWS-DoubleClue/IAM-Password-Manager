# REST API Documentation
This documentation details the creation process behind REST calls to DCEM.
Please read the document carefully before proceeding.

## How the REST API is created
For the generation of REST calls, we use a framework called "Swagger". 
This tool can auto-generate REST libraries (for clients and servers) with a number of Programming languages, such as Java and C#.
Our libraries' calls are defined in one file, `Doublelue.yaml`.
Any changes to the REST library must be done via this config file to prevent compatibility issues.
That being said, it is usually the case that auto-generated code must be edited later on to function properly.

## Creating new Elements
There are different types of elements in ``.yaml`` files.

There is a good [Documentation](https://swagger.io/docs/specification/2-0/basic-structure/) for Swagger where you
can find several examples. 
You can also find some useful examples in an [Online Editor](http://editor.swagger.io/#/) of Swagger,
where you can test many things, too.

See below for some examples from DCEM.

### Enums
Enums are very simple to implement. You can find them in the ``.yaml`` file when you search for ``definitions``.
They are all structured like the following example:
```
definitions:
  asApiMsgStatus:
    type: string
    enum: [Ok, Waiting, Queued, Sending, Disconnected, Cancelled, SignatureError]
```

### Model Classes
Model Classes define the objects that are passed as parameters or are part of the response data returned from calls.
```
asApiUrlToken:
   type: object
   properties:
     url:
       type: string
       required: true
     validMinutes:
       type: integer
       required: false
     username:
       type: string
       required: true
     token:
       type: string
       required: true
     urlTokenUsage:
       type: object
       $ref: "#/definitions/asApiUrlTokenUsage"
```

### Methods
Methods ultimately define calls, such as the following example:
```
/user/addUrlToken:
  post:
    description: request an url token for password reset
    operationId: addUrlToken
    produces:
    - application/json
    parameters:
    - name: urlToken
      in: body
      description: This is the Url Token object.
      required: true
      schema:
        $ref: '#/definitions/asApiUrlToken'
    responses:
      '200':
        description: OK returns
      '599':
        description: Server Logic Exception
        schema:
          $ref: '#/definitions/asApiException'
```

## Modifying existing elements
If you want to make some changes in the REST-Library, you just have to update the ``.yaml`` file.
Please keep in mind that you will have to build the whole library anew, both for the **Server** and **Client** side.

## Building the libraries
### Server side
Create a new configuration for external tools in eclipse. For the Arguments field, copy the following:
- location: ``C:\Program Files\Java\jdk1.8.0_171\bin\java.exe`` (or the path to your java)
- working Directory: ``${workspace_loc}``
- Arguments 
    ```
    Swagger 2.3   
    -jar ${workspace_loc}/SwaggerSpec/executable/swagger-codegen-cli-2.3.1.jar  generate 
    -i ${workspace_loc}/SwaggerSpec/src/DoubleClue.yaml -l jaxrs 
    -c ${workspace_loc}/SwaggerSpec/src/JavaConfigServer.json 
    -o ${workspace_loc}/Swagger-rest-server
    --language-specific-primitives boolean,int,long
    --type-mappings Boolean=boolean,Integer=int,Long=long
    
    OpenApi 3.0.0
    -jar ${workspace_loc}/SwaggerSpec/executable/openapi-generator-cli-5.1.0.jar  generate -g jaxrs-jersey
	-i ${workspace_loc}/SwaggerSpec/src/DoubleClue.yaml
	-c ${workspace_loc}/SwaggerSpec/src/JavaConfigServer.json 
	-o ${workspace_loc}/SwaggerServer
	--type-mappings Boolean=boolean,Integer=int,Long=long
    ```
After you have been started this application, all java classes for the server side will be created.    

### Client side
Create a new configuration for external tools in eclipse. For the Arguments field, copy the following:
- location: ``C:\Program Files\Java\jdk1.8.0_171\bin\java.exe`` (or the path to your java)
- Working Directory: ``${workspace_loc}``
- Arguments
    ```
    -jar ${workspace_loc}/SwaggerSpec/executable/swagger-codegen-cli-2.3.1.jar  generate 
    -i ${workspace_loc}/SwaggerSpec/src/DoubleClue.yaml -l java -c${workspace_loc}/SwaggerSpec/src/JavaConfigClient.json 
    -o ${workspace_loc}/Swagger-rest-client
    --language-specific-primitives boolean,int,long
    --type-mappings Boolean=boolean,Integer=int,Long=long
    ```
    
After this, copy the changed content into the existing code.
If you have created or changed model classes you have to copy these classes from the source directory
``.\swagger-rest-client\src\main\java\com\doubleclue\as\restapi\model`` to ``.\LibRestDcClient\src\com\doubleclue\as\restapi\model``

if you have changed or created some methods, you will have to copy the changed java code to the 
correct java class in ``LibDcRestClient``.

### Creating HTML Documentation
The documentation for the REST-library is also done automatically.
To create it you have to make a new configuration for running an external tool.
- location: ``C:\Program Files\Java\jdk1.8.0_171\bin\java.exe`` (or the path to your java)
- working Directory: ``${workspace_loc}``
- Arguments 
    ```
   -jar ${workspace_loc}/SwaggerSpec/executable/swagger-codegen-cli-2.3.1.jar  generate 
   -i ${workspace_loc}/SwaggerSpec/src/DoubleClue.yaml -l html2 
   -o ${workspace_loc}/DcemDistribution/artifacts/yajsw/doc/REST-WebServices
    ```
After running this application the documentation will be copied to the ``DcemDistribution`` directory.
This directory contains all project files which will be deployed to the customer.    
 
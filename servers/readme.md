# Pasos a seguir para levantar el server
## Instalar pyhton

## Instalar dependencia relacionada a grpc
### pip install grpcio grpcio-tools

## Instalar dotenv para configurar el archivo .env para configurar usuario y password para enviar mail con contraseña
### pip install python-dotenv

## Crear el archivo .env en la raiz del proyecto con las variables 
### SENDER_EMAIL=email
### SENDER_PASSWORD=password

## Posicionarse en el directorio 'ong-empuje-comunitario/' 

### para generar los archivos a partir del .proto:
#### python -m grpc_tools.protoc -I servers --python_out=servers --grpc_python_out=servers servers/servers.proto

### Para levantar el server
#### ejecutar: python -m servers.main
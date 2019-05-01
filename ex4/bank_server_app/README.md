# Instal
``` py 
> pip install grpc
> pip install grpcio-tools
```

python -m grpc_tools.protoc -I../grpc_protobuff --python_out=. --grpc_python_out=. ../grpc_protobuff/currency_tracker.proto
const PROTO_PATH = __dirname + '/../grpc_protobuff/currency_tracker.proto'
const grpc = require('grpc')
const protoLoader = require('@grpc/proto-loader')
const services = require('./services')

const HOST = "127.0.0.1"
const PORT = 50051

const packageDefinition = protoLoader.loadSync(
  PROTO_PATH,
  {keepCase: true,
   longs: String,
   enums: String,
   defaults: true,
   oneofs: true
  }
)

const protoDescriptor = grpc.loadPackageDefinition(packageDefinition)
const currency_tracker = protoDescriptor.currency_tracker

const server = new grpc.Server();
server.addService(currency_tracker.CurrencyTracker.service, {
  subscribe: services.subscription.subscribe,
  unsubscribe: services.subscription.unsubscribe
});

server.bind(HOST + ":" + PORT, grpc.ServerCredentials.createInsecure());
server.start()
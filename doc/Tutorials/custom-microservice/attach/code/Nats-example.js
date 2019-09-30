const { avroSchema } = require('./schema.js');
const NATS = require('nats');
const avro = require('avsc');

//Avro schema for TSTP protocol. More info: https://github.com/kaaproject/kaa-rfcs/blob/master/0014/README.md
const schema = avroSchema;
const nats = NATS.connect({'url': 'nats://localhost:4222', 'preserveBuffers': true});
const type = avro.parse(JSON.stringify(schema), {wrapUnions: true});
let subjectForSubscription = `kaa.v1.events.epts.endpoint.data-collection.data-points-received.Logs`;
//Listen for messages from EPTS
nats.subscribe(subjectForSubscription, function(msg) {
  console.log('Message received: ', type.toString(type.fromBuffer(msg)));
});
//Publish message to EPTS. 'test' part in subject stands for the name of TSTP transmitter configured.
//See EPTS docs for more information: https://docs.kaaiot.io/EPTS/docs/current/Overview/
const subjectForPublish = `kaa.v1.events.test.endpoint.data-collection.data-points-received.Logs`;
const message = {
  correlationId: Math.random().toString(36).substring(7),
  timestamp: 0,
  appVersionName: 'demo_application_v1',
  endpointId: '4f24cb40-282d-4dfb-8396-71e7a4782cb5', // You can get endpoint ID after its provisioning, see EPR REST API - https://docs.kaaiot.io/EPR/docs/current/REST-API/#endpoints_post_response
  timeSeriesName: 'Logs',
  dataPoints: [{
    timestamp: 1875943753,
    values:  new Map([['value','1']]),
  }]
};
const buf = type.toBuffer(message);
nats.publish(subjectForPublish, buf);

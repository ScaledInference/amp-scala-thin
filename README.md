# amp-scala-thin
Scala thin client for com.scaledinference.amp_v2.Amp

Two example files to illustrate usage of the api are provided.

<b>AmpSingleSession.scala</b>

This creates an amp object, a session object and makes a single call.
The objects are created with default values and use only the required parameters (such as the project Key for the amp object) to successfully create them.

<b>AmpMultipleSession.scala</b>
This creates an amp object, and two session objects and makes a single call on each objects.
The objects are created with non default values to override the api defaults.

The scala thin client itself can be compiled by running "sbt compile" or packaged by running "sbt package" from the command line in the directory containing this README file.

In order to run the example <b>AmpSingleSession</b> via a command line in the directory containing this README file

```
sbt "test:runMain com.scaledinference.amp_v2.AmpSingleSession 98f3c5cdb920c361 http://localhost:8100"

```
where `98f3c5cdb920c361` should be replaced with your project key, and `http://localhost:8100` should be replaced with the address of you amp-agent.

After running it, you should get output that looks something like

```
firstSession = Session(Amp(98f3c5cdb920c361,Vector(http://localhost:8100),10 seconds,30 minutes,false),y7DIcN7imEOrvbP3,y33qgotryZMyNkWP,20 seconds,30 minutes,)
Calling firstSession.decideWithContext, with a 3 seconds timeout
decisionAndToken = DecideResponse(Map(color -> red, count -> 10),Gk8FGg4GAExeRxpYICwRL1oZGSokFxgGNVBNSEcBDwwdEU1RRxdXVhIICxEAGDc9DSEAMj5GSUEfDwALQ1dSTVcNVg1RBgcNXVdCAl5GRU1HRx4QHBMKRl9QICAgMStJSUwXERBNXlRHVVlETFlSVFpVVlFDRgkbBwgEHQIOR1RVXVNfVFVCTU8ZGgsOHQsXR1kURlVFUV8UTV5bVghVBFtZXFERVwkSQlleVFcCBFZWVgdDQ1dACRI=,false,None)
Returned ampToken Gk8FGg4GAExeRxpYICwRL1oZGSokFxgGNVBNSEcBDwwdEU1RRxdXVhIICxEAGDc9DSEAMj5GSUEfDwALQ1dSTVcNVg1RBgcNXVdCAl5GRU1HRx4QHBMKRl9QICAgMStJSUwXERBNXlRHVVlETFlSVFpVVlFDRgkbBwgEHQIOR1RVXVNfVFVCTU8ZGgsOHQsXR1kURlVFUV8UTV5bVghVBFtZXFERVwkSQlleVFcCBFZWVgdDQ1dACRI= 
 of length 248
Returned decision: Map(color -> red, count -> 10)
Decision successfully obtained from amp-agent
Calling firstSession.observe with default timeout
Returned ampToken Gk8FGg4GAExeRxpYICwRL1oZGSokFxgGNVBNSEcBDwwdEU1RRxdXVhIICxEAGDc9DSEAMj5GSUEfDwALQ1dSTVcNVg1RBgcNXVdCAl5GRU1HRx4QHBMKRl9QICAgMStJSUwXERBNXlRHVVlETFlSVFpVVlFDRgkbBwgEHQIOR1RVXVNfVFVCTU8ZGgsOHQsXR1kURlVFUV8UTV5bVghVBFtZXFERVwkSQlleVFcCBFZWVgdDQ1dACRI= 
 of length 248
Observe successfully sent to amp-agent.

```

# First Time

- [ ] Install
[Docker](https://www.docker.com).
If running OSX, Docker
[1.13.1](https://download.docker.com/mac/stable/1.13.1.15353/Docker.dmg)
is compatible with the demo.
Newer versions might work but some conflicts have been found with Kubernetes.

- [ ] Install
[OpenShift Origin](https://www.openshift.org)
or
[Minishift](https://github.com/minishift/minishift).
If running OSX,
[OpenShift Origin 1.4.1](https://github.com/openshift/origin/releases/tag/v1.4.1)
is known to work with Docker version above.
The instructions below assume you're using OpenShift Origin.

- [ ] Install
[Maven 3](https://maven.apache.org/download.cgi).

- [ ] Install
[Kubetail](https://github.com/johanhaleby/kubetail).

- [ ] Install Jupyter via 
[Anaconda](https://www.continuum.io/downloads).

If doing live demos and running OSX, installing
[Gas Mask](https://github.com/2ndalpha/gasmask)
, a hosts file manager, is highly recommended.
Gas Mask enables you to easily override host files, making it easy to force routes to be re-routed correctly without requiring external DNS services. 


# Pre talk

- [ ] Check out `pre-early17` branch.

```bash
git checkout pre-early17
```

- [ ] Adjust `hostPath` in `datagrid/datagrid.yml` to point to the correct folder.

- [ ] Make sure Docker is running.

- [ ] Docker settings: 5 CPU, 8 GB

- [ ] Start OpenShift cluster:

```bash
oc cluster up --public-hostname=127.0.0.1
```

- [ ] Deploy all components:

```bash
./deploy-all.sh
```

You can follow progress of deployment of Infinispan server pods via:

```bash
kubetail -l cluster=datagrid
```

- [ ] Open Chrome and 
[verify all pods are running](https://127.0.0.1:8443/console/project/myproject/overview).

- [ ] Start Jupyter, open `live-demo.ipynb` and verify that the URL returns `0` results:

```bash
cd analytics/analytics-jupyter
~/anaconda/bin/jupyter notebook
```

- [ ] Clear Jupyter output by clicking: `Cell` / `All Output` / `Clear`


# Real Time Demo

- [ ] Start `delays.query.continuous.fx.FxApp` from IDE and show how no delays are being sent.

- [ ] Implement query in `delays.query.continuous.ContinuousQueryVerticle`:

```java
Query query = qf.from(StationBoard.class)
   .having("entries.delayMin").gt(0L)
   .build();
```

- [ ] Implement publishing delay in `delays.query.continuous.ContinuousQueryVerticle`:
  
```java
value.entries.stream()
   .filter(e -> e.delayMin > 0)
   .forEach(e -> {
      publishDelay(key, e);
});
```

- [ ] Add continuous query in `delays.query.continuous.ContinuousQueryVerticle`: 

```java
continuousQuery.addContinuousQueryListener(query, listener);
```

- [ ] Redeploy `delays.query.continuous.ContinuousQueryVerticle` verticle:

```bash
cd real-time/real-time-vertx
mvn clean package -DskipTests=true
oc start-build real-time --from-dir=. --follow
```

- [ ] Start `delays.query.continuous.fx.FxApp` from IDE and within a few seconds you should see delays appearing.


# Analytics Demo

- [ ] Go to Jupyter notebook, open `live-demo.ipynb` and verify that URL returns `0` entries.

- [ ] Implement delay ratio task in `delays.java.stream.task.DelayRatioTask` class:

```java
Map<Integer, Long> totalPerHour = cache.values().stream()
      .collect(
            serialize(() -> Collectors.groupingBy(
                  e -> getHourOfDay(e.departureTs),
                  Collectors.counting()
            )));

Map<Integer, Long> delayedPerHour = cache.values().stream()
      .filter(e -> e.delayMin > 0)
      .collect(
            serialize(() -> Collectors.groupingBy(
                  e -> getHourOfDay(e.departureTs),
                  Collectors.counting()
            )));
```

- [ ] Recompile and redeploy server task:

```bash
cd analytics
mvn clean package -pl analytics-server
yes | cp analytics-server/target/analytics-server-1.0-SNAPSHOT.jar ../datagrid/target/analytics-server.jar
```
    
- [ ] Go to Jupyter notebook and run each cell again of `live-demo.ipynb`.
Value for `analytics.size` should be 48.
The time with biggest ratio of delayed trains should be 2am.

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
[OpenShift Origin 3.7](https://github.com/openshift/origin/releases/tag/v3.7.0-rc.0)
is known to work with Docker version above.
The instructions below assume you're using OpenShift Origin.

- [ ] Install
[Maven 3](https://maven.apache.org/download.cgi).

- [ ] Install
[Kubetail](https://github.com/johanhaleby/kubetail).

- [ ] Install Jupyter via 
[Anaconda](https://www.continuum.io/downloads).

# Pre talk

- [ ] Adjust `hostPath` in `datagrid/datagrid.yml` to point to the correct folder.

- [ ] Make sure Docker is running.

- [ ] Docker settings: 5 CPU, 8 GB

- [ ] Start OpenShift cluster:

```bash
oc cluster down && oc cluster up
```

- [ ] Deploy data grid:

```bash
cd datagrid
./deploy.sh
```

You can follow progress of deployment of Infinispan server pods via:

```bash
kubetail -l cluster=datagrid
```

- [ ] Deploy analytics component:

```bash
cd ../analytics
./deploy.sh
```

- [ ] Open Chrome and 
[verify all pods are running](https://127.0.0.1:8443/console/project/myproject/overview).

- [ ] Start Jupyter, open `live-demo.ipynb` and verify that the URL returns `0` results:

```bash
cd analytics/analytics-jupyter
~/anaconda/bin/jupyter notebook
```

- [ ] Clear Jupyter output by clicking: `Cell` / `All Output` / `Clear`


# Analytics Demo

- [ ] Go to Jupyter notebook, open `live-demo.ipynb` and verify that URL returns `0` entries.

- [ ] Implement delay ratio task in `delays.java.stream.task.DelayRatioTask` class:

```java
Map<Integer, Long> totalPerHour = cache.values().stream()
      .collect(
            () -> Collectors.groupingBy(
                  e -> getHourOfDay(e.departureTs),
                  Collectors.counting()
            ));

Map<Integer, Long> delayedPerHour = cache.values().stream()
      .filter(e -> e.delayMin > 0)
      .collect(
            () -> Collectors.groupingBy(
                  e -> getHourOfDay(e.departureTs),
                  Collectors.counting()
            ));
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

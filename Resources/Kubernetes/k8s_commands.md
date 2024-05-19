## Minikube Commands
    minikube start
    minikube dashboard
    minikube stop

## Run a Shell From a Pod
    kubectl exec -it pod-name -- /bin/sh
    kubectl exec -it pod-name -c container-name -- /bin/sh

## Delete All Deployments
    kubectl delete --all deployments

## Configure Minikube Resources
    minikube stop
    minikube delete
    minikube config set memory 8192
    minikube config set cpus 4
    minikube start

## Open Service at Localhost
    minikube service kibana-service
    minikube service elasticsearch-service
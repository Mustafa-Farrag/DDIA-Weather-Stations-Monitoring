## Minikube Commands
    minikube start

    minikube dashboard

    minikube stop

## Run a Shell From a Pod
    kubectl exec -it pod-name -- /bin/sh

    kubectl exec -it pod-name -c container-name -- /bin/sh

## Delete All Deployments
    kubectl delete --all deployments
# Flags choisi
Xmx / Xms (type: Heap)

UseParallelGC (type: GC)


## Justification
### Xmx / Xms
Nous considerons ces 2 flags comme un seul puisqu'ils sont très semblables.
En effet, les 2 vont assister à la gestion du heap de mémoire.
Réduire la taille maximale du heap permettra de vérifier que les tests s'exécutent bien en simulant des appareils avec peu de ressources disponibles.
Dans un autre temps, réduire la taille initiale du heap permettra de voir si les tests s'exécutent adéquatement même s'ils doivent venir augmenter la taille du heap en raison de manque de mémoire.
L'inverse est aussi un aspect intéressant à tester ; s'assurer que les tests fonctionnent bien avec un heap initial d'une bonne taille qui n'a pas besoin d'augmenter.
Comme Jackson est une librairie de traitement de JSON, et qu'il est fréquent d'avoir des données lourdes à traiter, il est cruciale de tester les accès à la mémoire de l'appareil.

### UseParallelGC
Avec G1 étant le GC (garbage collector) par défaut, nous trouvions nécessaire de valider les tests avec un GC différent.
Le GC parallèle était celui par défaut pour les versions antérieurs à Java 9, et comme Jackson est disponible pour Java 8, il est important de tester avec ce GC.
Également, valider le bon fonctionnement d'un GC est cruciale pour une application intense en mémoire comme Jackson.
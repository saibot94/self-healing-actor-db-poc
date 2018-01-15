# Autonomous systems project

This is the implementation for `autonomous systems`, ALFP, year 2, West University of Timisoara.

# Task

See transcript below:


 Ma gandeam sa propun implementarea unei baze de date distribuite 
 care sa aiba urmatoarele doua proprietati ce se refera la sisteme complexe:

* fault tolerance / recovery: mai multe "noduri" care comunica 
intre ele si sunt coordonate de catre un master; in cazul in 
care unul dintre ele are probleme sau se defecteaza, el este 
fie restartat, fie se executa anumite actiuni pentru a-l readuce la stare normala
* replication: find o baza de date distribuita, datele sunt 
replicate pe mai multe dintre nodurile acestei baze 
de date, astfel incat chiar daca se pierde informatia dintr-unul 
dintre noduri, se poate regasi si pe celelalte. 


Din punct de vedere al implementarii, voiam sa folosesc un sistem de 
actori si un design reactiv, bazat pe operatii asincrone intr-un sistem de 
actori. Toate mesajele dintre actori vor trece printr-o coada de mesaje (Apache Kafka, ActiveMQ 
sau RabbitMQ). Pentru demonstrarea abilitatilor de mai sus as putea sa scriu un program ce testeaza 
functionalitatea sistemului, precum si operatiile de baza pe care le ofera (query dupa 
anumite date, stergere, adaugare).


# Implementation

## Teacher's notes:
Am inteles ce doriti sa realizati.
Sa proiectati arhitectura si doar cateva functiuni.
Am ca si intrebari:
* cum si cand se constata ca s-au pierdut date
* cine, cand si ce trebuie sa faca pentru recuperare

Astea la prezentare, sa nu le uitati.

## My notes

### Copyright

Cristian Schuszter, 2018
All rights reserved
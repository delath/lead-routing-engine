# Lead Routing Engine

Microservizio per l'assegnazione automatica dei lead agli agenti immobiliari.

## Come funziona

POST /v1/leads riceve i dati del cliente e dell'immobile, poi assegna il lead all'agente più adatto nella stessa città. L'agente viene scelto in base al carico di lavoro (chi ha meno lead nelle ultime 24h viene preferito).

## Scelte tecniche

### Locking pessimistico
Ho usato un lock pessimistico (SELECT FOR UPDATE) sugli agenti quando assegno un lead. Motivo: se due richieste arrivano insieme e un agente ha solo 1 slot libero, senza lock entrambe potrebbero assegnarglielo. Con il lock la seconda richiesta aspetta che la prima finisca.

### Capacità agenti
Ogni agente può avere massimo 5 lead nelle ultime 24h. Il conteggio viene fatto con una query sul DB invece di tenere un contatore sull'entità Agent - così il dato è sempre consistente e non devo preoccuparmi di aggiornare contatori.

### DB in-memory
Per l'esercizio uso H2. In produzione ovviamente PostgreSQL o simile.

## Scalabilità (se dovessi gestire migliaia di lead/sec)

Alcune idee:
- Mettere una coda (Kafka/SQS) davanti all'API per assorbire i picchi
- Partizionare per città: ogni istanza gestisce solo alcune città
- Se il sistema di notifica fallisce, il lead resta salvato sul DB. Un job asincrono può riprovare le notifiche fallite

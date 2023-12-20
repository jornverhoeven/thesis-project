class Secret {
    constructor(value) {
        this._value = value;
        this._seed = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);

        ["toString", "valueOf", "toJSON", "inspect", ].forEach(method => {
            this[method] = () => '[redacted]';
        }); 
        // For debugging purposes we can use a custom inspect method
        this[Symbol.for('nodejs.util.inspect.custom')] = () => createTartan(this._value, this._seed);
        Object.freeze(this);
    }

    release() {
        return this._value;
    }

    map(fn) {
        return new Secret(fn(this._value));
    }
    
    static wrapSecrets(object, keys) {
        if (!keys) {
            keys = Object.keys(object);
        }
        return keys.reduce((acc, key) => {
            if (!(object[key] instanceof Secret)) {
                return Object.assign(acc, { [key]: new Secret(object[key]) });
            } else {
                return Object.assign(acc, { [key]: object[key] });
            }
        }, {});
    }
}

function createTartan(value, seed) {
    let tartan = "";
    let hash = 0;
    for (let i = 0, len = value.length; i < len; i++) {
        let chr = value.charCodeAt(i);
        let s = parseInt(seed.charCodeAt(i % seed.length)) % 16;
        hash = ((hash << 5) - s) + chr;
        tartan += String.fromCodePoint(0x2580 + (hash % 0x20));
    }
    return tartan;
}

class SecretEnvironment {
    constructor() {
        this._secrets = [];

        ["toString", "valueOf", "toJSON", "inspect", ].forEach(method => {
            this[method] = () => this._output();
        }); 
        // For debugging purposes we can use a custom inspect method
        var sym = Symbol.for('nodejs.util.inspect.custom');
        this[sym] = () => this._secrets.reduce((acc, { name, secret }) => {
            const n = name instanceof Secret ? name[sym]() : name;   
            const s = secret instanceof Secret ? secret[sym]() : secret;
            return Object.assign(acc, { [n]: s });
        });
        Object.freeze(this);
    }

    addSecret(name, secret) {
        const _secret = secret instanceof Secret ? secret : new Secret(secret);
        this._secrets.push({ name, secret: _secret });
    }

    _output() {
        let secretKeyCount = 0;
        return this._secrets.reduce((acc, { name, secret }) => {
            if (name instanceof Secret) secretKeyCount++;
            return Object.assign(acc, { [name+secretKeyCount]: secret });
        }, {});
    }
}

// module.exports = Secret;


const object = {
    something: 'value',
    else: new Secret("test")
}

console.log(object);
console.log(object.else.release());
console.log(object.else.map(x => x.toUpperCase));
console.log(object.else.map(x => x.toUpperCase()).release());

const test = { 
    hello: "there",
    some: "value",
    else: new Secret("test"),
    else2: new Secret("test"),
}
console.log(Secret.wrapSecrets(test));


const env = new SecretEnvironment();
env.addSecret("test", "test");
env.addSecret("test2", "test2");
env.addSecret("test3", "test");
env.addSecret(new Secret("secret-key"), new Secret("secret-value"));
env.addSecret(new Secret("secret-key2"), new Secret("secret-value2"));

console.log(env);
console.log(JSON.stringify(env));

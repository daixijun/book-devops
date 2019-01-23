pipeline {
    agent any

    parameters {
        booleanParam(defaultValue: true, description: '', name: 'flag')

        // soon to be changed to stringParam
        string(name: 'Greeting', defaultValue: 'Hello', description: 'How should I greet the world?')
        string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
    }

    stages {
        stage('Example') {
            steps {
                echo "${Greeting} World!"
                echo "Hello ${params.PERSON}"
            }
        }
    }
}
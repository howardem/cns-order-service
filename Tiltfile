# OS Identification
gradlew = "./gradlew"
expected_ref = "$EXPECTED_REF"

if os.name == "nt":
  gradlew = "gradlew.bat"
  expected_ref = "%EXPECTED_REF%"

# Build
custom_build(
    # Name of the container image
    ref = 'cns-order-service',
    tag = 'latest',
    # Command to build the container image
    command = 'gradlew bootBuildImage --imageName ' + expected_ref,
    # Files to watch that trigger a new build
    deps = ['build.gradle', 'src']
#	deps=['/build/classes', 'build.gradle'],
#    live_update = [
#    	sync('/build/classes/java/main', '/app/classes')
#	]
)

# Deploy
k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])

# Manage
k8s_resource('order-service', port_forwards=['9002'])
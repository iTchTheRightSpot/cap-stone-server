# using latest GraalVM for java 21 as per https://github.com/graalvm/container/pkgs/container/graalvm-community
# https://www.graalvm.org/latest/docs/getting-started/container-images/
FROM ghcr.io/graalvm/graalvm-community:21 AS builder

# set the working directory name to build
WORKDIR /build

# copy the app source code into build directory
COPY . /build

# compile to native image
RUN ./mvnw clean --no-transfer-progress -Pnative native:compile -DskipTests

# stage 2 aws linux OS
FROM public.ecr.aws/amazonlinux/amazonlinux:2023

# update OS
RUN yum update -y

COPY --from=builder /build/target/capstone-server ./

# set permission
RUN chmod +x ./

# command to run bytecode
CMD ["./capstone-server"]
# Optimus Toolshed - Distribution and Maintenance

## Distribution Strategy

Optimus Toolshed utilizes a hybrid distribution model that combines self-contained executable deployment with modular plugin management to deliver both deployment simplicity and operational flexibility. The primary distribution is a Windows MSI installer containing a self-contained executable with embedded JVM runtime, eliminating Java installation dependencies while providing comprehensive component selection during setup. The installer includes the core application framework and a curated set of default management tools, with additional plugins available as optional components that administrators can select based on their specific Optimus Server management requirements. This approach ensures immediate usability after installation while maintaining the flexibility to customize the tool suite for different operational environments and deployment scenarios.

## Plugin Architecture and Updates

Individual management tools are distributed as separate JAR libraries that integrate with the core application through standardized plugin APIs, enabling dynamic discovery and loading without requiring application restarts for most operations. Each plugin maintains its own release cycle and version management, with updates delivered through direct downloads from the project's Bitbucket repository using secure HTTPS distribution channels. The plugin system supports both automatic update checking and manual update management, allowing administrators to control when and how new tool versions are deployed in their environments. Plugin metadata includes compatibility information, dependency requirements, and change notifications, ensuring that updates maintain system stability while providing access to enhanced functionality and bug fixes as they become available.

## Maintenance and Support Strategy

The maintenance approach separates core application updates from plugin updates to minimize disruption and reduce bandwidth requirements for ongoing system management. Core application updates deliver new self-contained executables through traditional software update mechanisms, preserving existing plugin configurations and data while upgrading the underlying framework and JVM runtime. Plugin updates operate independently, replacing individual JAR files without affecting other tools or requiring full application reinstallation, enabling rapid deployment of fixes and feature enhancements. The update system includes rollback capabilities for both core and plugin updates, automated integrity verification for downloaded components, and configurable update policies that accommodate enterprise change management processes and security requirements.

---

*Document Version: 1.0*  
*Last Updated: June 30, 2025*  
*Purpose: Distribution and maintenance strategy for implementation planning*
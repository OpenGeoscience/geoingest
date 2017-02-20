from setuptools import setup

setup(name='ingest',
      version='0.0',
      description='Ingest data to geotrellis catalog',
      author='Doruk Ozturk',
      author_email='doruk.ozturk@kitware.com',
      license='Apache 2.0',
      packages=['ingest'],
      zip_safe=False,
      entry_points={
          'console_scripts': [
              "ingest=ingest.ingest:main"
          ]
      }
)
